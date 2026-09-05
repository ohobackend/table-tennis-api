package com.tabletennis.app.domain.user.dto;
import java.time.LocalDate;
public record UserProfile(Integer userId,String userName,String realName,String clubName,String gender,
    Integer openRanking,Integer regionRanking,String userType,String profileImage) {}
