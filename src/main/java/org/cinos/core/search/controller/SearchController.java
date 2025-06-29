package org.cinos.core.search.controller;

import lombok.RequiredArgsConstructor;
import org.cinos.core.search.dto.SearchResultDTO;
import org.cinos.core.search.service.impl.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<List<SearchResultDTO>> search(@RequestParam final String q) {
        List<SearchResultDTO> results = searchService.search(q);
        return ResponseEntity.ok(results);
    }

}
