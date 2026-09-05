package com.tabletennis.app.domain.ranking.dto;
public record PlayerStats(Integer userId,String userName,String realName,String clubName,String gender,long totalMatches,long wins,double winRate,double averagePoints) {}
