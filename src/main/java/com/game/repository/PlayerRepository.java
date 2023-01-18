package com.game.repository;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
//import javax.annotation.PreDestroy;


import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static java.util.Objects.isNull;


@Repository
public class PlayerRepository{
    private final SessionFactory sessionFactory;

    public PlayerRepository() {
        Properties properties = new Properties();
        properties.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
        properties.put(Environment.URL, "jdbc:mysql://localhost:3306/rpg");
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        properties.put(Environment.HBM2DDL_AUTO, "update");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "root");
        sessionFactory = new Configuration()
                .setProperties(properties)
                .addAnnotatedClass(Player.class)
                .buildSessionFactory();
    }

    public List<Player> getAll(String playerName, String playerTitle, Date after, Date before,
                               Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel,
                               String playerRace, String playerProfession, String playerBanned,
                               int pageNumber, int pageSize, String fieldOrder) {
        try (Session session = sessionFactory.openSession()) {

            String nameCondition = "p.name like :playerName ";
            String titleCondition = "and p.title like :playerTitle ";
            String birthdayCondition = "and p.birthday between :after and :before ";
            String experienceCondition =  "and p.experience between :minExperience and :maxExperience ";
            String levelCondition =   "and p.level between :minLevel and :maxLevel ";
            String raceCondition =  playerRace.equals("ANY") ? "" : "and p.race=:playerRace ";
            String professionCondition =  playerProfession.equals("ANY") ? "" : "and p.profession=:playerProfession ";
            String bannedCondition =  playerBanned.equals("ANY") ? "" : "and p.banned=:playerBanned ";

            String hql = "from Player as p where " +
                    nameCondition +
                    titleCondition +
                    birthdayCondition +
                    experienceCondition +
                    levelCondition +
                    raceCondition +
                    professionCondition +
                    bannedCondition +
                    "order by "+ fieldOrder + " asc";
            Query<Player> query = session.createQuery(hql, Player.class);
            query.setParameter("playerName", "%"+playerName+"%");
            query.setParameter("playerTitle", "%"+playerTitle+"%");
            query.setParameter("after", after);
            query.setParameter("before", before);
            query.setParameter("minExperience", minExperience);
            query.setParameter("maxExperience", maxExperience);
            query.setParameter("minLevel", minLevel);
            query.setParameter("maxLevel", maxLevel);
            if (!playerRace.equals("ANY")){
                query.setParameter("playerRace", Race.valueOf(playerRace));
            }
            if (!playerProfession.equals("ANY")){
                query.setParameter("playerProfession", Profession.valueOf(playerProfession));
            }
            if (!playerBanned.equals("ANY")){
                query.setParameter("playerBanned", Boolean.parseBoolean(playerBanned));
            }
            query.setFirstResult(pageNumber * pageSize);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getAllCount(String playerName, String playerTitle, Date after, Date before,
                           Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel,
                           String playerRace, String playerProfession, String playerBanned) {
        try (Session session = sessionFactory.openSession()) {
            String nameCondition = "p.name like :playerName ";
            String titleCondition = "and p.title like :playerTitle ";
            String birthdayCondition = "and p.birthday between :after and :before ";
            String experienceCondition =  "and p.experience between :minExperience and :maxExperience ";
            String levelCondition =   "and p.level between :minLevel and :maxLevel ";
            String raceCondition =  playerRace.equals("ANY") ? "" : "and p.race=:playerRace ";
            String professionCondition =  playerProfession.equals("ANY") ? "" : "and p.profession=:playerProfession ";
            String bannedCondition =  playerBanned.equals("ANY") ? "" : "and p.banned=:playerBanned ";

            String hql = "sel" +
                    "ect count(*) from Player as p where " +
                    nameCondition +
                    titleCondition +
                    birthdayCondition +
                    experienceCondition +
                    levelCondition +
                    raceCondition +
                    professionCondition+
                    bannedCondition;
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("playerName", "%"+playerName+"%");
            query.setParameter("playerTitle", "%"+playerTitle+"%");
            query.setParameter("after", after);
            query.setParameter("before", before);
            query.setParameter("minExperience", minExperience);
            query.setParameter("maxExperience", maxExperience);
            query.setParameter("minLevel", minLevel);
            query.setParameter("maxLevel", maxLevel);
            if (!playerRace.equals("ANY")){
                query.setParameter("playerRace", Race.valueOf(playerRace));
            }
            if (!playerProfession.equals("ANY")){
                query.setParameter("playerProfession", Profession.valueOf(playerProfession));
            }
            if (!playerBanned.equals("ANY")){
                query.setParameter("playerBanned", Boolean.parseBoolean(playerBanned));
            }
            return Math.toIntExact(query.uniqueResult());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public Integer calcLevel(Integer experience){
        if (isNull(experience) || (experience<0)){
            experience=0;
        }
        return (int)(Math.sqrt(2500+200*experience)-50)/100;
    }

    public Integer calcNextLevel(Integer level, Integer experience){
        if (isNull(experience) || (experience<0)){
            experience=0;
        }
        if (isNull(level) || (level<0)){
            level=0;
        }
        return 50*(level+1)*(level+2)-experience;
    }

    public Player save(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            Integer level = calcLevel(player.getExperience());
            Integer untilNextLevel = calcNextLevel(level, player.getExperience());
            player.setLevel(level);
            player.setUntilNextLevel(untilNextLevel);
            session.saveOrUpdate(player);
            transaction.commit();
            return player;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Player update(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            Integer level = calcLevel(player.getExperience());
            Integer untilNextLevel = calcNextLevel(level, player.getExperience());
            player.setLevel(level);
            player.setUntilNextLevel(untilNextLevel);
            session.update(player);
            transaction.commit();
            return player;
        }
    }

    public Optional<Player> findById(long id) {
        try (Session session = sessionFactory.openSession()) {
            Player player =  session.find(Player.class,id);
            return Optional.ofNullable(player);
        }
    }

    public void delete(Player player) {
            try (Session session = sessionFactory.openSession()) {
                Transaction transaction = session.beginTransaction();
                session.remove(player);
                transaction.commit();
            }
        }

/*    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }*/
}