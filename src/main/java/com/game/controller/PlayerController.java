package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@RestController
@RequestMapping("/rest/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(@Autowired PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping()
    public List<PlayerInfo> getAll(@RequestParam(required = false) String name,
                                   @RequestParam(required = false) String title,
                                   @RequestParam(required = false) String race,
                                   @RequestParam(required = false) String profession,
                                   @RequestParam(required = false) Long after,
                                   @RequestParam(required = false) Long before,
                                   @RequestParam(required = false) Integer minExperience,
                                   @RequestParam(required = false) Integer maxExperience,
                                   @RequestParam(required = false) Integer minLevel,
                                   @RequestParam(required = false) Integer maxLevel,
                                   @RequestParam(required = false) String banned,
                                   @RequestParam(required = false) Integer pageNumber,
                                   @RequestParam(required = false) Integer pageSize,
                                   @RequestParam(required = false) String order) {
        name = isNull(name) ? "": name;
        title = isNull(title) ? "": title;
        after = isNull(after) ? 0L: after;
        before = isNull(before) ? new Date().getTime(): before;
        minExperience = isNull(minExperience) ? 0: minExperience;
        maxExperience = isNull(maxExperience) ? Integer.MAX_VALUE: maxExperience;
        minLevel = isNull(minLevel) ? 0: minLevel;
        maxLevel = isNull(maxLevel) ? Integer.MAX_VALUE: maxLevel;
        race = isNull(race) ? "ANY" : race;
        profession = isNull(profession) ? "ANY" : profession;
        banned = isNull(banned) ? "ANY" : banned;
        pageNumber = isNull(pageNumber) ? 0 : pageNumber;
        pageSize = isNull(pageSize) ? 3 : pageSize;
        order = isNull(order) ? "ID": order;

        List<Player> players = playerService.getAll(name, title, after, before, minExperience, maxExperience, minLevel, maxLevel,
                race, profession, banned, pageNumber, pageSize, order);
        return players.stream().map(PlayerController::toPlayerInfo).collect(Collectors.toList());
    }

    @GetMapping("/{ID}")
    public ResponseEntity<PlayerInfo> getPlayer(@PathVariable("ID") long id) {
        if (id <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        Player player = playerService.getById(id);
        if (isNull(player)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(toPlayerInfo(player));
        }
    }

    @GetMapping("/count")
    public Integer getAllCount(@RequestParam(required = false) String name,
                               @RequestParam(required = false) String title,
                               @RequestParam(required = false) String race,
                               @RequestParam(required = false) String profession,
                               @RequestParam(required = false) Long after,
                               @RequestParam(required = false) Long before,
                               @RequestParam(required = false) Integer minExperience,
                               @RequestParam(required = false) Integer maxExperience,
                               @RequestParam(required = false) Integer minLevel,
                               @RequestParam(required = false) Integer maxLevel,
                               @RequestParam(required = false) String banned) {
        name = isNull(name) ? "": name;
        title = isNull(title) ? "": title;
        after = isNull(after) ? 0L: after;
        before = isNull(before) ? new Date().getTime(): before;
        minExperience = isNull(minExperience) ? 0: minExperience;
        maxExperience = isNull(maxExperience) ? Integer.MAX_VALUE: maxExperience;
        minLevel = isNull(minLevel) ? 0: minLevel;
        maxLevel = isNull(maxLevel) ? Integer.MAX_VALUE: maxLevel;
        race = isNull(race) ? "ANY" : race;
        profession = isNull(profession) ? "ANY" : profession;
        banned = isNull(banned) ? "ANY" : banned;
        return playerService.getAllCount(name, title, after, before, minExperience, maxExperience, minLevel, maxLevel,
                race, profession, banned);
    }

    @PostMapping
    public ResponseEntity<PlayerInfo> createPlayer(@RequestBody PlayerInfo info) {
        if (StringUtils.isEmpty(info.name) || info.name.length() > 12) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        if (info.title.length() > 30) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        if (isNull(info.race)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        if (isNull(info.profession)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        Long currentDate = new Date().getTime();
        if (isNull(info.birthday) || info.birthday < 0 || info.birthday>=currentDate) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        LocalDate localDate = new Date(info.birthday).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int year = localDate.getYear();
        if (year < 2000 || year > 3000) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        boolean banned = !isNull(info.banned) && info.banned;

        if (isNull(info.experience) || info.experience < 0 || info.experience>10000000) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        Player player = playerService.createPlayer(info.name, info.title, info.race, info.profession, info.birthday, banned, info.experience);
        return ResponseEntity.status(HttpStatus.OK).body(toPlayerInfo(player));
    }

    @PostMapping("/{ID}")
    public ResponseEntity<PlayerInfo> updatePlayer(@PathVariable("ID") long id,
                                                   @RequestBody PlayerInfo info) {
        if (checkEmptyInfo(info)) return ResponseEntity.status(HttpStatus.OK).body(toPlayerInfo(playerService.getById(id)));
        if (id <= 0) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        Player playerStored = playerService.getById(id);
        if(isNull(playerStored)) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        info = mergePlayerInfoWithPlayer(info,playerStored);
        if (nonNull(info.name) && (info.name.length() > 12 || info.name.isEmpty())) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        if (nonNull(info.title) && info.title.length() > 30) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        if (isNull(info.race)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        if (isNull(info.profession)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        Long currentDate = new Date().getTime();
        if (isNull(info.birthday) || info.birthday < 0 || (info.birthday>=currentDate)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        LocalDate localDate = new Date(info.birthday).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int year = localDate.getYear();
        if (year < 2000 || year > 3000) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        info.banned = !isNull(info.banned) && info.banned;
        if (isNull(info.experience) || (info.experience<0 || info.experience > 10000000)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);


        Player playerUpdated = playerService.updatePlayer(id, info);
        if (isNull(playerUpdated)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(toPlayerInfo(playerUpdated));
        }
    }

    public PlayerInfo mergePlayerInfoWithPlayer(PlayerInfo info, Player player){
        if(isNull(info.name))  info.name = player.getName();
        if(isNull(info.title))  info.title = player.getTitle();
        if(isNull(info.race))  info.race = player.getRace();
        if(isNull(info.profession))  info.profession = player.getProfession();
        if(isNull(info.birthday))  info.birthday = player.getBirthday().getTime();
        if(isNull(info.experience))  info.experience = player.getExperience();
        return info;
    }

    @DeleteMapping("/{ID}")
    public ResponseEntity delete(@PathVariable("ID") long id) {
        if (id <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
/*        if (isNull(playerService.getById(id))){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }*/
        Player player = playerService.delete(id);
        if (isNull(player)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
    }

    private static PlayerInfo toPlayerInfo(Player player) {
        if (isNull(player)) return null;

        PlayerInfo result = new PlayerInfo();
        result.id = player.getId();
        result.name = player.getName();
        result.title = player.getTitle();
        result.race = player.getRace();
        result.profession = player.getProfession();
        result.birthday = player.getBirthday().getTime();
        result.banned = player.getBanned();
        result.experience = player.getExperience();
        result.level = player.getLevel();
        result.untilNextLevel = player.getUntilNextLevel();
        return result;
    }

    private Boolean checkEmptyInfo(PlayerInfo info){
        //if (isNull(info)) return true;
        return isNull(info.id) &&
                isNull(info.name) &&
                isNull(info.title) &&
                isNull(info.race) &&
                isNull(info.profession) &&
                isNull(info.birthday) &&
                isNull(info.banned) &&
                isNull(info.experience) &&
                isNull(info.level) &&
                isNull(info.untilNextLevel);
    }
}