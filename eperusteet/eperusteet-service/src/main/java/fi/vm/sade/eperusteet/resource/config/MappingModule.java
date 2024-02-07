package fi.vm.sade.eperusteet.resource.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public class MappingModule extends SimpleModule {

    public MappingModule() {
        super(MappingModule.class.getSimpleName());
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.setMixInAnnotations(Page.class, PageMixin.class);
        context.setMixInAnnotations(PageImpl.class, PageMixin.class);
    }

    @JsonIgnoreProperties(value = {"numberOfElements", "firstPage", "lastPage", "sort", "first", "last"})
    public static abstract class PageMixin {

        @JsonProperty("data")
        abstract List<?> getContent();

        @JsonProperty("sivu")
        abstract int getNumber();

        @JsonProperty("sivuja")
        abstract int getTotalPages();

        @JsonProperty("kokonaismäärä")
        abstract int getTotalElements();

        @JsonProperty("sivukoko")
        abstract int getSize();

    }

}
