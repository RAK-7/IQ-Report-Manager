package IQ_Report_Manager.factory.filehandler;

import IQ_Report_Manager.filehandler.FileHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FileHandlerFactory {

    @Autowired
    private List<FileHandler> handlers;

    public FileHandler getHandler(String type) {

        return handlers.stream()
                .filter(h -> h.getType().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("Unsupported file type: " + type)
                );
    }
}