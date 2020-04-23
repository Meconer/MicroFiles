/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package microfiles;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import javafx.concurrent.Task;

/**
 *
 * @author mats.andersson
 */
class FileList {
    
    private ArrayList<String> fileList = new ArrayList<>();
    
    FileList(String startPathString, String[] extensions) {
        Path startPath = Paths.get(startPathString);
        try {
            Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
                    
                    String fileString = file.toAbsolutePath().toString();
                    String fileStringLower = fileString.toLowerCase();
                    if (extensions.length > 0 ) {
                        for ( String extension: extensions) {

                            if ( fileStringLower.contains(extension)) {
                                fileList.add(fileString);
                            }
                        }
                    } else {
                        fileList.add(fileString);
                    }
                    return FileVisitResult.CONTINUE;
                }
                
                
            });
        } catch (Exception ex) {
            System.out.println("Fel vid läsning av " + startPathString);
        }
    }

    public int getListSize() {
        return fileList.size();
    }

    public ArrayList<String> getStringList() {
        return fileList;
    }

}
