package com.example.demo.url;
import com.example.demo.url.Exceptions.InvalidUrlException;
import com.example.demo.url.Solutions.UrlCheck;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping(path = "/api")
public class UrlController {
    private final UrlRepository URLREPO;
    private String checkUrlValidity(String url) {
        return UrlCheck.check(url) ? "VALID" : "INVALID";
    }
    private final UrlService URLSERVICE;
    private static final Logger LOG = LoggerFactory.getLogger(UrlController.class);
    @Autowired
    public UrlController(UrlRepository URLREPO, UrlService URLSERVICE) {
        this.URLREPO = URLREPO;
        this.URLSERVICE = URLSERVICE;
    }
    @GetMapping(path = "{id}")      // To find the whole data for the url
    public List<Map<String, String>> getUrlById(@PathVariable("id") String id) {
        String url = URLSERVICE.getUrlById(id);
        Long remainingTtl = URLSERVICE.getRemainingTtlById(id);
        List<Map<String, String>> urlDetails = new ArrayList<>();
        Map<String, String> details = new HashMap<>();
        details.put("URL", url);
        details.put("ID", id);
        details.put("TTL_LEFT", remainingTtl == null ? "STATIC" : String.valueOf(remainingTtl));
        details.put("DATE", String.valueOf(URLSERVICE.getDateById(id)));
        urlDetails.add(details);
        return urlDetails;
    }
    @GetMapping("/urls")        // get a list full of saved urls
    public ResponseEntity<List<Map<String, Object>>> getUrls() {
        List<Url> urls = URLSERVICE.getUrls();
        List<Map<String, Object>> urlDetails = new ArrayList<>();
        for (Url url : urls) {
            Map<String, Object> details = new HashMap<>();
            details.put("ID", url.getID());
            details.put("URL", url.getUrl());
            details.put("TTL_LEFT", url.getRemainingTtl() == null ? "STATIC" : url.getRemainingTtl());
            details.put("DATE", url.getDATE());
            urlDetails.add(details);
        }
        return ResponseEntity.ok(urlDetails);
    }

    @PostMapping    // post new urls with optional ttl or id
    public ResponseEntity<Map<String, Object>> registerNewUrl(@RequestBody Url url) {
        if (url.getUrl() == null){throw new IllegalArgumentException("NO URL GIVEN");}
        String valid = checkUrlValidity(url.getUrl());
        Url createdUrl = URLSERVICE.addNewUrl(url);
        Map<String, Object> urlDetails = new HashMap<>();
        urlDetails.put("STATUS", "CREATED");
        urlDetails.put("ID", createdUrl.getID());
        urlDetails.put("URL", createdUrl.getUrl());
        urlDetails.put("TTL", createdUrl.getRemainingTtl() == null ? "STATIC" : (createdUrl.getRemainingTtl() < 0 ? "NEGATIVE" : createdUrl.getRemainingTtl())); // if it happens to be negative
        urlDetails.put("DATE", createdUrl.getDATE());
        if (url.getTTL() != null) {
            LOG.info(url.getTTL() + " SECONDS TTL GIVEN, " + valid + " URL " + url.getUrl() + " WITH ID " + createdUrl.getID() + " CREATED");
        }
        return ResponseEntity.ok(urlDetails);
    }
    @DeleteMapping(path = "{id}")   // delet urls per id
    public ResponseEntity<Map<String, Object>> deleteUrl(@PathVariable("id") String id) {
        Map<String, Object> urlDetails = new HashMap<>();
        urlDetails.put("STATUS", "DELETED");
        urlDetails.put("ID", id);
        urlDetails.put("URL", URLSERVICE.getUrlById(id));
        urlDetails.put("DATE_OF_CREATION", URLSERVICE.getDateById(id));
        LOG.info(URLSERVICE.getUrlById(id) + " URL WITH ID " + id + " DELETED");
        URLSERVICE.deleteUrl(id);
        return ResponseEntity.ok(urlDetails);
    }
    @PutMapping(path = "{id}")  // change the url by given id
    public ResponseEntity<Map<String, Object>> updateUrl(@PathVariable("id") String id, @RequestParam(required = false) String url) {
        Map<String, Object> urlDetails = new HashMap<>();
        if (url == null){
            urlDetails.put("STATUS","ILLEGAL PUT OPERATION");
            return ResponseEntity.ok(urlDetails);
        }
        String oldurl = URLSERVICE.getUrlById(id);
        urlDetails.put("STATUS", "CHANGED");
        urlDetails.put("ID", id);
        urlDetails.put("NEW_URL", url);
        urlDetails.put("DATE_OF_CREATION", URLSERVICE.getDateById(id));
        urlDetails.put("MSG",oldurl+" CHANGE TO "+url);
        URLSERVICE.updateUrl(id,url);
        LOG.info(oldurl+" CHANGE TO "+url);
        return ResponseEntity.ok(urlDetails);
    }

    // how I add schemas?
    @Operation(summary = "Change all URL properties by given ID")
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Successful operation", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)) }),
    @ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
    @ApiResponse(responseCode = "404", description = "URL not found", content = @Content) })
    @PatchMapping(path = "{id}")
    public ResponseEntity<Map<String, Object>> patchUrl(@PathVariable("id") String id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Patches to the URL", content =
        @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = Url.class)))
        @org.springframework.web.bind.annotation.RequestBody Url updates) {
        Map<String, Object> urlDetails = new HashMap<>();
        String oldId = id;
        String oldUrl = URLSERVICE.getUrlById(id);
        Long oldTtl = URLSERVICE.getRemainingTtlById(id);
        // le fix XD
        Url newUrl = new Url();
        if (updates.getID() != null){
            newUrl.setID(updates.getID());
            oldId = updates.getID();
        }else {
            newUrl.setID(oldId);}
        if (updates.getUrl() != null){
            if (UrlCheck.check(updates.getUrl())) {
            throw new InvalidUrlException("Wrong URL");}
            newUrl.setUrl(updates.getUrl());
            oldUrl = updates.getUrl();
        }else {
            newUrl.setUrl(oldUrl);}
        if (updates.getTTL() != null){
            newUrl.setTTL(updates.getTTL());
            oldTtl = updates.getRemainingTtl();
        }else {
            newUrl.setTTL(oldTtl);}
        URLSERVICE.deleteUrl(id);
        Url createdUrl = URLSERVICE.addNewUrl(newUrl);

        urlDetails.put("STATUS", "PATCHED");
        urlDetails.put("SET_ID", oldId);
        urlDetails.put("SET_URL", oldUrl);
        urlDetails.put("SET_TTL", oldTtl);
        urlDetails.put("DATE_OF_CREATION", createdUrl.getDATE());
        LOG.info("CHANGES FOR ID " + id + " APPLIED");
        return ResponseEntity.ok(urlDetails);
    }

}