package IQ_Report_Manager.factory.publisher;

import IQ_Report_Manager.publisher.Publisher;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PublisherFactory {

    private final Map<String, Publisher> publishers = new HashMap<>();

    public PublisherFactory(List<Publisher> publisherList){

        for(Publisher publisher : publisherList){
            publishers.put(publisher.getPublisherType(),publisher);
        }
    }

    public Publisher getPublisher(String type){

        Publisher publisher = publishers.get(type);

        if(publisher == null){
            throw new RuntimeException("Publisher not found: "+type);
        }

        return publisher;
    }
}
