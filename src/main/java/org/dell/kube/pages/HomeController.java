package org.dell.kube.pages;

import ch.qos.logback.classic.Logger;
import feign.FeignException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class HomeController {

    @Autowired
    CategoryClient categoryClient;
    private IPageRepository pageRepository;
    private String pageContent;
    Logger logger =(Logger) LoggerFactory.getLogger(this.getClass());

    public HomeController(@Value("${page.content}") String pageContent, IPageRepository pageRepository){
        this.pageContent=pageContent;
        this.pageRepository = pageRepository;
    }

    @GetMapping
    public String getPage(){
        return "Hello from page : "+pageContent+" ";
    }

    @PostMapping
    public ResponseEntity<Page> create(@RequestBody Page page) {

        logger.info("CREATE-INFO:Creating a new page");
        logger.debug("CREATE-DEBUG:Creating a new  page");
        Category category = null;
        try {
            category = categoryClient.findCategory(page.getCategoryId());
        }
        catch(FeignException ex){
            if(ex.getMessage().contains("404")) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            else{
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        if(category ==null || category.getId()==null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else
        {
            Page newPage = pageRepository.create(page);
            logger.info("CREATE-INFO:Created a new page with id = " + newPage.id);
            logger.debug("CREATE-DEBUG:Created a new  page with id = " + newPage.id);
            return new ResponseEntity<Page>(newPage, HttpStatus.CREATED);
        }
    }
}