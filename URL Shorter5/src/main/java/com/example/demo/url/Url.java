package com.example.demo.url;

import com.example.demo.url.Solutions.UrlHash;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table
@Schema(description = "Url details")
public class Url {
    private static final Logger LOG = LoggerFactory.getLogger(UrlService.class);

    @Id
    @Schema(description = "The unique id of the URL", example = "1A", required = false)
    private String ID;

    public Url() {
        this.ID = UrlHash.hashTime();  // set own hash
        this.DATE = LocalDateTime.now();   // get local time for timestamp
    }

    @Schema(description = "The actual URL", example = "http://example.com", required = true)
    private String url;

    public void setTTL(Long TTL) {
        if(TTL != null){
            if(TTL < 0){
                this.TTL = Math.abs(TTL);
                LOG.error("NEGATIVE INVERTED");
            }else {
                this.TTL = TTL;
            }
        }else {
            this.TTL = null;
        }
    }



    @Schema(description = "The TTL of the URL", example = "3600", required = false)
    private Long TTL;

    @JsonIgnore
    private LocalDateTime DATE;
    @JsonIgnore
    public Long getRemainingTtl() {
        if (TTL == null) {
            return null;
        } else if (TTL < 0) {
            return null;
        }
        Duration duration = Duration.between(DATE, LocalDateTime.now());
        long secondsPassed = duration.getSeconds();
        return TTL - secondsPassed;
    }
    @JsonIgnore
    public LocalDateTime getDATE() {return DATE;}

    public String getID() {return ID;}
    public void setID(String newId) {this.ID = newId;}
    public String getUrl() {return url;}
    public void setUrl(String url) {this.url = url;}
    public Long getTTL() {
        if (TTL != null){return Math.abs(TTL);}
        else {return null;}}

    @Override
    public String toString() {
        return "Url{" +
                "id=" + ID +
                ", url='" + url +
                ", ttl=" + TTL +
                ", date=" + DATE +
                '}';
    }

}
