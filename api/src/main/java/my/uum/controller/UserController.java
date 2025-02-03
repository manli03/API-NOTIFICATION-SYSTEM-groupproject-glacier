package my.uum.controller;

import my.uum.entity.NetflixTitle;
import my.uum.repository.NetflixTitleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private NetflixTitleRepository netflixTitleRepository;

    @GetMapping("/titles")
    public Page<NetflixTitle> getAllTitles(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "100") int size) {
        return netflixTitleRepository.findAll(PageRequest.of(page, size));
    }

    // New endpoint to return all titles without pagination
    @GetMapping("/all-titles")
    public List<NetflixTitle> getAllTitles() {
        return netflixTitleRepository.findAll();
    }

    @GetMapping("/test")
    public String testDatabaseConnection() {
        return "Netflix-Title Database connection test successful!";
    }

    @GetMapping("/titles/{show_id}")
    public ResponseEntity<NetflixTitle> getTitleByShowId(@PathVariable String show_id) {
        Optional<NetflixTitle> title = netflixTitleRepository.findById(show_id);
        return title.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/titles")
    public ResponseEntity<?> createTitle(@RequestBody NetflixTitle title) {
        try {
            // Detailed logging
            System.out.println("Received title: " + title);

            if (title.getShow_id() == null) {
                throw new IllegalArgumentException("show_id must be provided");
            }

            NetflixTitle savedTitle = netflixTitleRepository.save(title);
            return ResponseEntity.ok(savedTitle);
        } catch (Exception e) {
            // Log the error
            System.err.println("Error saving title: " + e.getMessage());
            e.printStackTrace();
            // Return a meaningful error response
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }


    @PutMapping("/titles/{show_id}")
    public ResponseEntity<NetflixTitle> updateTitle(@PathVariable String show_id, @RequestBody NetflixTitle titleDetails) {
        Optional<NetflixTitle> optionalTitle = netflixTitleRepository.findById(show_id);
        if (optionalTitle.isPresent()) {
            NetflixTitle title = optionalTitle.get();
            title.setType(titleDetails.getType());
            title.setTitle(titleDetails.getTitle());
            title.setDirector(titleDetails.getDirector());
            title.setCast(titleDetails.getCast());
            title.setCountry(titleDetails.getCountry());
            title.setDate_added(titleDetails.getDate_added());
            title.setRelease_year(titleDetails.getRelease_year());
            title.setRating(titleDetails.getRating());
            title.setDuration(titleDetails.getDuration());
            title.setListed_in(titleDetails.getListed_in());
            title.setDescription(titleDetails.getDescription());
            return ResponseEntity.ok(netflixTitleRepository.save(title));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/titles/{show_id}")
    public ResponseEntity<Void> deleteTitle(@PathVariable String show_id) {
        if (netflixTitleRepository.existsById(show_id)) {
            netflixTitleRepository.deleteById(show_id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
