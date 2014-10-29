/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.reporting.template.core.util.common;

public class FontStyleDTO {
    private String fontColor;
    private int fontSize;
    private String fontName;
    private boolean bold;
    private boolean italic;
    private boolean underLine;
    private boolean strikeThough;
    private String backgroundColour;
    private String alignment;

    public FontStyleDTO(){
        fontColor = "#000000";
        fontSize = 12;
        fontName  = "Arial";
        bold = false;
        italic = false;
        underLine = false;
        strikeThough = false;
        backgroundColour = "#FFFFFF";
        alignment = "Left";
    }
    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public boolean isUnderLine() {
        return underLine;
    }

    public void setUnderLine(boolean underLine) {
        this.underLine = underLine;
    }

    public String getBackgroundColour() {
        return backgroundColour;
    }

    public void setBackgroundColour(String backgroundColour) {
        this.backgroundColour = backgroundColour;
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public boolean isStrikeThough() {
        return strikeThough;
    }

    public void setStrikeThough(boolean strikeThough) {
        this.strikeThough = strikeThough;
    }

    public boolean equals(Object obj) {
            if(obj instanceof FontStyleDTO){
            FontStyleDTO anotherFont = (FontStyleDTO)obj;
            boolean isEqual = fontColor.equalsIgnoreCase(anotherFont.getFontColor()) &
                    (fontSize == anotherFont.getFontSize()) &
                    fontName.equalsIgnoreCase(anotherFont.getFontName()) &
                    (bold == anotherFont.isBold()) &
                    italic == anotherFont.isItalic() &
                    underLine == anotherFont.isUnderLine() &
                    strikeThough == anotherFont.isStrikeThough() &
                    backgroundColour.equalsIgnoreCase(anotherFont.getBackgroundColour()) &
                    alignment.equalsIgnoreCase(anotherFont.getAlignment());
            return isEqual;
            }
            else{
                return false;
            }
    }


}
