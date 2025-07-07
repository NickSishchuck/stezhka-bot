package com.NickSishchuck.StezhkaBot.repository;

import com.NickSishchuck.StezhkaBot.entity.TextContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TextContentRepository extends JpaRepository<TextContent, Long> {

    @Query("SELECT tc.textValue FROM TextContent tc WHERE tc.textKey = :textKey")
    Optional<String> getTextByName(@Param("textKey") String textKey);

    Optional<TextContent> findByTextKey(String textKey);

    boolean existsByTextKey(String textKey);
}