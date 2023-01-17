package com.game.service;

import com.game.controller.PlayerInfo;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;


import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;

    public PlayerService(@Autowired PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public List<Player> getAll(String playerName, String playerTitle, Long after, Long before,
                               Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel,
                               String playerRace, String playerProfession, String banned, int pageNumber, int pageSize, String fieldOrder) {
        return playerRepository.getAll(playerName, playerTitle, new Date(after), new Date(before),
                minExperience, maxExperience, minLevel, maxLevel, playerRace, playerProfession, banned,
                pageNumber, pageSize, fieldOrder);
    }

    public Player getById(long id) {
        return playerRepository.findById(id).orElse(null);
    }

    public Integer getAllCount(String playerName, String playerTitle, Long after, Long before,
                               Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel,
                               String playerRace, String playerProfession, String playerBanned) {
        return playerRepository.getAllCount(playerName, playerTitle, new Date(after), new Date(before),
                minExperience, maxExperience, minLevel, maxLevel, playerRace, playerProfession, playerBanned);
    }

    public Player createPlayer(String name, String title, Race race, Profession profession, long birthday, boolean banned, Integer experience) {
        Player player = new Player();
        player.setName(name);
        player.setTitle(title);
        player.setRace(race);
        player.setProfession(profession);
        player.setBirthday(new Date(birthday));
        player.setBanned(banned);
        player.setExperience(experience);

        return playerRepository.save(player);
    }

    public Player updatePlayer(long id, PlayerInfo info) {
        Player player = playerRepository.findById(id).orElse(null);
        if (isNull(player)) {
            return null;
        }

        boolean needUpdate = false;

        if (!StringUtils.isEmpty(info.name) && info.name.length() <= 12) {
            player.setName(info.name);
            needUpdate = true;
        }
        if (!StringUtils.isEmpty(info.title) && info.title.length() <= 30) {
            player.setTitle(info.title);
            needUpdate = true;
        }
        if (nonNull(info.race)) {
            player.setRace(info.race);
            needUpdate = true;
        }
        if (nonNull(info.profession)) {
            player.setProfession(info.profession);
            needUpdate = true;
        }

        if (nonNull(info.experience)) {
            player.setExperience(info.experience);
            needUpdate = true;
        }
        if (nonNull(info.birthday)) {
            player.setBirthday(new Date(info.birthday));
            needUpdate = true;
        }

        if (nonNull(info.banned)) {
            player.setBanned(info.banned);
            needUpdate = true;
        }

        if (needUpdate) {
            playerRepository.update(player);
        }

        return player;
    }

    public Player delete(long id) {
        Player player = playerRepository.findById(id).orElse(null);
        if (isNull(player)) {
            return null;
        }
        playerRepository.delete(player);
        return player;
    }
}
