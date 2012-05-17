/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.parser.microsoft.ooxml;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.POIXMLProperties.CoreProperties;
import org.apache.poi.POIXMLProperties.CustomProperties;
import org.apache.poi.POIXMLProperties.ExtendedProperties;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.poi.openxml4j.util.Nullable;
import org.apache.poi.xssf.extractor.XSSFEventBasedExcelExtractor;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Office;
import org.apache.tika.metadata.PagedText;
import org.apache.tika.metadata.Property;
import org.apache.tika.metadata.TikaCoreProperties;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty;
import org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.CTProperties;

/**
 * OOXML metadata extractor.
 * 
 * Currently POI doesn't support metadata extraction for OOXML.
 * 
 * @see OOXMLExtractor#getMetadataExtractor()
 */
public class MetadataExtractor {

    private final POIXMLTextExtractor extractor;

    public MetadataExtractor(POIXMLTextExtractor extractor) {
        this.extractor = extractor;
    }

    public void extract(Metadata metadata) throws TikaException {
        if (extractor.getDocument() != null ||
              (extractor instanceof XSSFEventBasedExcelExtractor && 
               extractor.getPackage() != null)) {
            extractMetadata(extractor.getCoreProperties(), metadata);
            extractMetadata(extractor.getExtendedProperties(), metadata);
            extractMetadata(extractor.getCustomProperties(), metadata);
        }
    }

    private void extractMetadata(CoreProperties properties, Metadata metadata) {
        PackagePropertiesPart propsHolder = properties
                .getUnderlyingProperties();

        addProperty(metadata, Metadata.CATEGORY, propsHolder.getCategoryProperty());
        addProperty(metadata, Metadata.CONTENT_STATUS, propsHolder
                .getContentStatusProperty());
        addProperty(metadata, TikaCoreProperties.DATE, propsHolder
                .getCreatedProperty());
        addProperty(metadata, TikaCoreProperties.CREATION_DATE, propsHolder
                .getCreatedProperty());
        addProperty(metadata, TikaCoreProperties.CREATOR, propsHolder
                .getCreatorProperty());
        addProperty(metadata, TikaCoreProperties.AUTHOR, propsHolder
                .getCreatorProperty());
        addProperty(metadata, TikaCoreProperties.DESCRIPTION, propsHolder
                .getDescriptionProperty());
        addProperty(metadata, TikaCoreProperties.IDENTIFIER, propsHolder
                .getIdentifierProperty());
        addProperty(metadata, TikaCoreProperties.KEYWORDS, propsHolder
                .getKeywordsProperty());
        addProperty(metadata, TikaCoreProperties.LANGUAGE, propsHolder
                .getLanguageProperty());
        addProperty(metadata, TikaCoreProperties.LAST_AUTHOR, propsHolder
                .getLastModifiedByProperty());
        addProperty(metadata, TikaCoreProperties.PRINT_DATE, propsHolder
                .getLastPrintedProperty());
        addProperty(metadata, Metadata.LAST_MODIFIED, propsHolder
                .getModifiedProperty());
        addProperty(metadata, TikaCoreProperties.MODIFIED, propsHolder
              .getModifiedProperty());
        addProperty(metadata, Metadata.REVISION_NUMBER, propsHolder
                .getRevisionProperty());
        addProperty(metadata, TikaCoreProperties.SUBJECT, propsHolder
                .getSubjectProperty());
        addProperty(metadata, TikaCoreProperties.TITLE, propsHolder.getTitleProperty());
        addProperty(metadata, Metadata.VERSION, propsHolder.getVersionProperty());
    }

    private void extractMetadata(ExtendedProperties properties,
            Metadata metadata) {
        CTProperties propsHolder = properties.getUnderlyingProperties();

        addProperty(metadata, Metadata.APPLICATION_NAME, propsHolder
                .getApplication());
        addProperty(metadata, Metadata.APPLICATION_VERSION, propsHolder
                .getAppVersion());
        addProperty(metadata, Office.CHARACTER_COUNT, propsHolder
                .getCharacters());
        addProperty(metadata, Office.CHARACTER_COUNT_WITH_SPACES, propsHolder
                .getCharactersWithSpaces());
        addProperty(metadata, TikaCoreProperties.PUBLISHER, propsHolder.getCompany());
        addProperty(metadata, Office.LINE_COUNT, propsHolder.getLines());
        addProperty(metadata, Metadata.MANAGER, propsHolder.getManager());
        addProperty(metadata, Metadata.NOTES, propsHolder.getNotes());
        addProperty(metadata, Office.PAGE_COUNT, propsHolder.getPages());
        if (propsHolder.getPages() > 0) {
            metadata.set(PagedText.N_PAGES, propsHolder.getPages());
        } else if (propsHolder.getSlides() > 0) {
            metadata.set(PagedText.N_PAGES, propsHolder.getSlides());
        }
        addProperty(metadata, Office.PARAGRAPH_COUNT, propsHolder.getParagraphs());
        addProperty(metadata, Metadata.PRESENTATION_FORMAT, propsHolder
                .getPresentationFormat());
        addProperty(metadata, Office.SLIDE_COUNT, propsHolder.getSlides());
        addProperty(metadata, Metadata.TEMPLATE, propsHolder.getTemplate());
        addProperty(metadata, Metadata.TOTAL_TIME, propsHolder.getTotalTime());
        addProperty(metadata, Office.WORD_COUNT, propsHolder.getWords());
    }

    private void extractMetadata(CustomProperties properties,
          Metadata metadata) {
       org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperties
           props = properties.getUnderlyingProperties();

       for(CTProperty property : props.getPropertyList()) {
          String val = null;
          Date date = null;

          if (property.isSetLpwstr()) {
             val = property.getLpwstr(); 
          }
          else if (property.isSetLpstr()) {
             val = property.getLpstr(); 
          }
          else if (property.isSetDate()) {
             date = property.getDate().getTime(); 
          }
          else if (property.isSetFiletime()) {
             date = property.getFiletime().getTime(); 
          }

          else if (property.isSetBool()) {
             val = Boolean.toString( property.getBool() );
          }

          // Integers
          else if (property.isSetI1()) {
             val = Integer.toString(property.getI1()); 
          }
          else if (property.isSetI2()) {
             val = Integer.toString(property.getI2()); 
          }
          else if (property.isSetI4()) {
             val = Integer.toString(property.getI4()); 
          }
          else if (property.isSetI8()) {
             val = Long.toString(property.getI8()); 
          }
          else if (property.isSetInt()) {
             val = Integer.toString( property.getInt() ); 
          }

          // Unsigned Integers
          else if (property.isSetUi1()) {
             val = Integer.toString(property.getUi1()); 
          }
          else if (property.isSetUi2()) {
             val = Integer.toString(property.getUi2()); 
          }
          else if (property.isSetUi4()) {
             val = Long.toString(property.getUi4()); 
          }
          else if (property.isSetUi8()) {
             val = property.getUi8().toString(); 
          }
          else if (property.isSetUint()) {
             val = Long.toString(property.getUint()); 
          }

          // Reals
          else if (property.isSetR4()) {
             val = Float.toString( property.getR4() ); 
          }
          else if (property.isSetR8()) {
             val = Double.toString( property.getR8() ); 
          }
          else if (property.isSetDecimal()) {
             BigDecimal d = property.getDecimal();
             if (d == null) {
                val = null;
             } else {
                val = d.toPlainString();
             }
          }

          else if (property.isSetArray()) {
             // TODO Fetch the array values and output
          }
          else if (property.isSetVector()) {
             // TODO Fetch the vector values and output
          }

          else if (property.isSetBlob() || property.isSetOblob()) {
             // TODO Decode, if possible
          }
          else if (property.isSetStream() || property.isSetOstream() ||
                   property.isSetVstream()) {
             // TODO Decode, if possible
          }
          else if (property.isSetStorage() || property.isSetOstorage()) {
             // TODO Decode, if possible
          }
          
          else {
             // This type isn't currently supported yet, skip the property
          }
          
          String propName = "custom:" + property.getName();
          if (date != null) {
             Property tikaProp = Property.externalDate(propName);
             metadata.set(tikaProp, date);
          } else if (val != null) {
             metadata.set(propName, val);
          }
       }
    }
    
    private <T> void addProperty(Metadata metadata, Property property, Nullable<T> nullableValue) {
        T value = nullableValue.getValue();
        if (value != null) {
            if (value instanceof Date) {
                metadata.set(property, (Date) value);
            } else if (value instanceof String) {
                metadata.set(property, (String) value);
            } else if (value instanceof Integer) {
                metadata.set(property, (Integer) value);
            } else if (value instanceof Double) {
                metadata.set(property, (Double) value);
            }
        }
    }

    private void addProperty(Metadata metadata, String name, Nullable<?> value) {
        if (value.getValue() != null) {
            addProperty(metadata, name, value.getValue().toString());
        }
    }
    
    private void addProperty(Metadata metadata, Property property, String value) {
        if (value != null) {
            metadata.set(property, value);
        }
    }

    private void addProperty(Metadata metadata, String name, String value) {
        if (value != null) {
            metadata.set(name, value);
        }
    }

    private void addProperty(Metadata metadata, Property property, int value) {
       if (value > 0) {
           metadata.set(property, value);
       }
    }
    
    private void addProperty(Metadata metadata, String name, int value) {
        if (value > 0) {
            metadata.set(name, Integer.toString(value));
        }
    }
}
