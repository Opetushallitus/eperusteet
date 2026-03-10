-- Function to recursively lowercase JSON object keys for case-insensitive jsonpath queries.
-- Preserves string values; only modifies key names.
CREATE OR REPLACE FUNCTION jsonb_lower_keys(obj jsonb)
RETURNS jsonb
LANGUAGE plpgsql
IMMUTABLE
AS $$
BEGIN
    IF obj IS NULL THEN
        RETURN NULL;
    ELSIF jsonb_typeof(obj) = 'object' THEN
        RETURN (
            SELECT jsonb_object_agg(lower(key), jsonb_lower_keys(value))
            FROM jsonb_each(obj)
        );
    ELSIF jsonb_typeof(obj) = 'array' THEN
        RETURN (
            SELECT jsonb_agg(jsonb_lower_keys(elem))
            FROM jsonb_array_elements(obj) elem
        );
    ELSE
        RETURN obj;
    END IF;
END;
$$;
