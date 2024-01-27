package com.example.demo.url;

import com.example.demo.url.Exceptions.*;
import com.example.demo.url.Solutions.UrlCheck;
import com.example.demo.url.Solutions.UrlHash;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class UrlService {
    private static final Logger LOG = LoggerFactory.getLogger(UrlService.class); //slf4j.log
    private final UrlRepository URLREPO;    // URL repository
    private final ScheduledExecutorService EXECSERVICE; // Executer service
    @Autowired
    public UrlService(UrlRepository URLREPO) {          // Constructor
        this.URLREPO = URLREPO;
        this.EXECSERVICE = Executors.newScheduledThreadPool(1);
    }
    private String checkUrlValidity(String url) { return UrlCheck.check(url) ? "VALID" : "INVALID";}  // for some reason
    public List<Url> getUrls() {
        return new ArrayList<>(URLREPO.findAll());
    }   // returns a list of all URLs
    ConcurrentHashMap<String, Long> URLTTLS = new ConcurrentHashMap<>();        // Using Concurrence to manage TTLs
    ConcurrentHashMap<String, Future<?>> URLTASKS = new ConcurrentHashMap<>();  // To stop execution process
    public Url addNewUrl(Url url) {
        if (!UrlCheck.check(url.getUrl())){ // Checks if its a valid url it should be "x.yz"
            throw new InvalidUrlException("URL "+ url.getUrl() +" IS NOT VALID");
        }
        if (url.getID() != null && URLREPO.findById(url.getID()).isPresent()) {     // checks if the url is already registered
            throw new IdAlreadyRegisteredException("ID " + url.getID() + " ALREADY REGISTERED BY "+url.getUrl());
        }
        if (url.getID() == null) {  // if no ttl given it gives automatically with my time hash generator
            LOG.info("NO ID GIVEN HASH GENERATED");
            url.setID(UrlHash.hashTime());
        }
        if (URLREPO.existsByUrl(url.getUrl())) {    // check if the URL is already registered
            LOG.error("URL " + url.getUrl() + " WITH ID " + getIdByUrl(url.getUrl()) + " ALREADY REGISTERED");
            throw new UrlAlreadyRegisteredException("URL " + url.getUrl() + " WITH ID " + getIdByUrl(url.getUrl()) + " ALREADY REGISTERED");
        }
        Url savedUrl = URLREPO.save(url);
        Long ttl = url.getTTL();
        handleTtl(savedUrl, ttl);   // if ttl is given URL is proceeding to ttlhandler
        return savedUrl;
    }
    private void handleTtl(Url url, Long ttl) {
        String valid = checkUrlValidity(String.valueOf(url.getUrl()));
        if (ttl != null) { // Check if ttl is not null
            if ( ttl <= 0){ // Check if ttl is less than or equal to 0
                LOG.warn("TTL IS LESS THAN OR EQUAL TO ZERO, "+valid+" URL "+url.getUrl()+" DELETED");
                URLREPO.deleteById(url.getID()); // delete the URL from the repository
                if (URLTASKS.containsKey(url.getID())) { // cancel the task and remove it from URLTASKS
                    URLTASKS.get(url.getID()).cancel(false);
                    URLTASKS.remove(url.getID());
                }
            } else{
                URLTTLS.put(url.getID(), ttl); // Put the ttl into URLTTLS with the URL ID as the key
                Future<?> task = EXECSERVICE.scheduleAtFixedRate(() -> { // schedule a task to be executed at a fixed rate of one second
                    long currentTTL = URLTTLS.get(url.getID()); // Get the current ttl for the URL
                    if (currentTTL > 0) { // Decrease the ttl by 1 if it's more than 0
                        URLTTLS.put(url.getID(), currentTTL - 1);
                    } else if (currentTTL == 0 || url.getRemainingTtl() == 0) {
                        LOG.info("URL "+getUrlById(url.getID())+" WITH ID "+url.getID()+" DELETED"); // Delete the URL from the repository
                        URLREPO.deleteById(url.getID());
                        if (URLTASKS.containsKey(url.getID())) { // cancel the task and remove it from URLTASKS
                            URLTASKS.get(url.getID()).cancel(false);
                            URLTASKS.remove(url.getID());
                        }
                    }
                }, 1, 1, TimeUnit.SECONDS);

                URLTASKS.put(url.getID(), task); // save the task in URLTASKS with the URL ID as the key
            }
        } else {
            LOG.warn("NO TTL DETECTED, {} URL {} WITH ID {} STAY FOREVER",valid, url.getUrl(), url.getID());
        }
    }



    public String getIdByUrl(String url) { // name
        return URLREPO.findIdByUrl(url)
                .orElseThrow(() -> new EntityNotFoundException("URL " + url + " NOT FOUND"));
    }
    public String getUrlById(String id) {
        return URLREPO.findById(id)
                .map(Url::getUrl)
                .orElseThrow(() -> {
                    LOG.error("URL WITH ID {} NOT FOUND OR WAS DELETED", id);
                    return new EntityNotFoundException("URL WITH ID " + id + " NOT FOUND OR WAS DELETED");
                });
    }
    public LocalDateTime getDateById(String id){
        return URLREPO.findById(id)
                .map(Url::getDATE)
                .orElseThrow(() -> {
                    LOG.error("URL WITH ID {} NOT FOUND OR WAS DELETED", id);
                    return new EntityNotFoundException("URL WITH ID " + id + " NOT FOUND OR WAS DELETED");
                });
    }
    public void deleteUrl(String id) {  // delete if called log if deleted ok?:) but it works.
        if (!URLREPO.existsById(id)){
            LOG.warn("URL "+getUrlById(id)+" WITH ID "+id+" DELETED");
        } else {
            URLREPO.deleteById(id);
        }
    }
    @Transactional
    public void updateUrl(String id, String newUrl) {
        String oldurl = getUrlById(id);
        Url existingUrl = URLREPO.findById(id).orElseThrow(() -> new EntityNotFoundException("URL WITH ID "+id+" NOT FOUND"));
        if (URLREPO.existsByUrl(newUrl)) {
            LOG.error("URL {} ALREADY EXISTS WITH ID {}",newUrl,id);
            throw new UrlAlreadyRegisteredException("URL "+newUrl+" ALREADY EXISTS WITH ID "+id);
        }
        if (newUrl == null) { // FIXING PUT PROBLEM checks if the url is null and then delete it.
            LOG.error("NULL URL DETECTED, CAUSE ILLEGAL PUT OPERATION");
            newUrl = oldurl;
            existingUrl.setUrl(newUrl);
            URLREPO.save(existingUrl);

        }else {
            LOG.info("URL {} WITH ID {} CHANGED TO {}",getUrlById(id),id,newUrl);
            existingUrl.setUrl(newUrl);
            URLREPO.save(existingUrl);}

    }


    @Transactional
    public void updateId(String oldId, String newId) {
        Url url = URLREPO.findById(oldId).orElseThrow(() -> new EntityNotFoundException("URL WITH ID "+oldId+" NOT FOUND"));
        if (URLREPO.existsById(newId)) {
            LOG.error("ID {} ALREADY EXISTS WITH URL {}",newId,url.getUrl());
            throw new IdAlreadyRegisteredException("ID "+newId+" ALREADY EXISTS WITH URL "+url.getUrl());
        }
        Url newUrl = new Url();
        newUrl.setID(newId);
        newUrl.setUrl(url.getUrl());
        newUrl.setTTL(url.getRemainingTtl());
        URLREPO.save(newUrl);
        URLREPO.deleteById(oldId);
    }

    @Transactional
    public void updateTtl(String id, Long newTtl) {
        Url url = URLREPO.findById(id).orElseThrow(() -> new EntityNotFoundException("URL WITH ID "+id+" NOT FOUND"));
        if (newTtl != null) {
            if(newTtl <= 0){
                LOG.info("ID "+id+" DELETED");
                URLREPO.deleteById(id);
            }else{
                handleTtl(url, newTtl);
                url.setTTL(newTtl);
            }
        } else {
            url.setTTL(null);
        }
    }



    public Long getRemainingTtlById(String id) {
        return URLREPO.findById(id).get().getRemainingTtl();
    } // It works I don't care
    public Long getTtlById(String id) {
        return URLREPO.findById(id).get().getTTL();} // if it works dont change.
}