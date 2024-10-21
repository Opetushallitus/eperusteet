#!/bin/bash

# Fetch the list of available products from the Endoflife API
curl -s https://endoflife.date/api/all.json -o available_products.json

# Extract Maven dependencies (groupId:artifactId:version) into a text file
mvn dependency:list -DoutputFile=dependencies.txt -DincludeScope=runtime

# Initialize an array to store results
result_array=()

# Parse the dependencies and loop through each
grep ":.*:.*:.*" dependencies.txt | while IFS= read -r line; do
  # Split the dependency into parts: groupId, artifactId, and version
  group_id=$(echo "$line" | cut -d':' -f1 | xargs)  # Trim spaces
  artifact_id=$(echo "$line" | cut -d':' -f2 | xargs)  # Trim spaces
  version=$(echo "$line" | cut -d':' -f4 | xargs)  # Trim spaces

  # Extract major.minor version (first two numbers) for comparison
  major_minor_version=$(echo "$version" | cut -d'.' -f1,2)

  # Determine the product name based on groupId
  product_name=""
  if [[ "$group_id" == "org.springframework.boot" ]]; then
    product_name="spring-boot"
  elif [[ "$group_id" == "org.springframework" ]]; then
    product_name="spring-framework"
  else
    product_name="$artifact_id"  # Use artifactId for other dependencies
  fi

  # Initialize EOL information
  eol_status=""

  # If the product is spring-boot or spring-framework, fetch EOL data
  eol_data=$(curl -s "https://endoflife.date/api/$product_name.json" 2>/dev/null)

  echo "processing: $group_id|$artifact_id|$version"

  # Check if eol_data is valid JSON before processing
  if echo "$eol_data" | jq empty 2>/dev/null; then
    # Check if eol_data is valid and extract relevant details
    if [[ $(echo "$eol_data" | jq 'type') == '"array"' ]]; then
      # Iterate over the cycles in the product's EOL data to find a match for the version
      # Use jq to output the cycle data as a single line
      for cycle_data in $(echo "$eol_data" | jq -c '.[] | {cycle, eol}'); do
        # Add debug output to check cycle_data
        #echo "Debug: Processing cycle_data: $cycle_data"  # Debug line
        
        # Extract fields from the current cycle
        cycle=$(echo "$cycle_data" | jq -r '.cycle // empty')
        eol=$(echo "$cycle_data" | jq -r '.eol // empty')
        
		# Check for parsing errors or empty values
        if [[ -z "$cycle" || -z "$eol" ]]; then
          echo "Warning: Missing cycle or eol in cycle_data: $cycle_data"  # Debug warning
          continue  # Skip this iteration
        fi
        
		# If the cycle (major.minor version) matches the dependency's major.minor version
        if [[ "$cycle" == "$major_minor_version" ]]; then
          eol_status="$eol"
          break
        fi
      done
    else
      echo "Error: eol_data is not an array for product: $product_name"  # Debug error
    fi
  else
    echo "Error: Invalid JSON from EOL API for product: $product_name"
  fi

  result_array+=("$group_id|$artifact_id|$version|$eol_status")
  
  printf "%s\n" "${result_array[@]}" | sort | awk -F'|' '{printf "| %s | %s | %s | %s |\n", $1, $2, $3, $4}'
done

# Print the header for the output table
echo "|| Group ID || Artifact || Version || EOL Status ||"

# Sort results by Group ID and print them in the desired format
printf "%s\n" "${result_array[@]}" | sort | awk -F'|' '{printf "| %s | %s | %s | %s |\n", $1, $2, $3, $4}'
