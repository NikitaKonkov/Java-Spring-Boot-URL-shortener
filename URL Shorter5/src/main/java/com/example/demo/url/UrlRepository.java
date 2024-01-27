package com.example.demo.url;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


@Repository
public interface UrlRepository extends JpaRepository<Url, String> {
    Optional<Url> findById(String id);
    @Query("SELECT u.id FROM Url u WHERE u.url = :url")
    Optional<String> findIdByUrl(@Param("url") String url);
    boolean existsByUrl(String url); //check by url
    boolean existsById(String id); //check by id
}