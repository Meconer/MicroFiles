/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package microfiles;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author mats.andersson
 */
public class FileSet {
    private final SimpleStringProperty filePath = new SimpleStringProperty("PathName");
    private final SimpleStringProperty fileName = new SimpleStringProperty("FileName");

    public String getPathName() {
        return filePath.get();
    }

    public void setPathName(String filePath) {
        this.filePath.set(filePath);
    }

    public String getFileName() {
        return fileName.get();
    }
    
    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }


}
