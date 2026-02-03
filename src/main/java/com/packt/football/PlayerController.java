package com.packt.football;

import com.packt.football.exceptions.NotFoundException;
import com.packt.football.model.Player;
import com.packt.football.service.FootballService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.channels.AlreadyBoundException;
import java.util.List;

@RequestMapping("/players")
@RestController
public class PlayerController {
    private FootballService footballService;

    private PlayerController(FootballService footballService) {
        this.footballService = footballService;
    }

    @GetMapping
    public List<Player> listPlayers() {
        return footballService.listPlayers();
    }

//    @GetMapping("/{id}")
//    public Player readPlayer(@PathVariable String id) {
//        return footballService.getPlayer(id);
//    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> readPlayer(@PathVariable String id) {
        try {
            Player player = footballService.getPlayer(id);
            return new ResponseEntity<>(player, HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public void createPlayer(@RequestBody Player player) {
        footballService.addPlayer(player);
    }

    @DeleteMapping("/{id}")
    public void deletePlayer(@PathVariable String id) {
        footballService.deletePlayer(id);
    }

    @PutMapping("/{id}")
    public Player updatePlayer(@PathVariable String id, @RequestBody Player player) {
        return footballService.updatePlayer(player);
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Not found")
    @ExceptionHandler(NotFoundException.class)
    public void notFoundHandler() {

    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Already exist")
    @ExceptionHandler(AlreadyBoundException.class)
    public void alreadyExistHandler() {

    }
}
