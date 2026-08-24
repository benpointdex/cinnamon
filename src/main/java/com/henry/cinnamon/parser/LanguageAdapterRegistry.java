package com.henry.cinnamon.parser;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Component
public class LanguageAdapterRegistry {

    private final List<LanguageAdapter> adapters;

    public LanguageAdapterRegistry( List<LanguageAdapter> adapters){
        this.adapters = adapters;
    }


    public Optional<LanguageAdapter> forFile(String fileName){

        String extension =getExtention( fileName);
        return adapters.stream().filter(adapters->adapters.supports(extension)).findFirst();
    }

    private String getExtention(String fileName) {
        if(fileName == null || fileName.isBlank()){

            return "";
        }

        int lastDot = fileName.lastIndexOf(".");
        if(lastDot==-1 || lastDot == fileName.length() - 1){
            return "";
        }
        return fileName.substring(lastDot+1);

    }


}
