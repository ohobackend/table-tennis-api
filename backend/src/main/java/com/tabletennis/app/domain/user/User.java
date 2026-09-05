package com.tabletennis.app.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.*;
import java.math.BigDecimal;
import com.tabletennis.app.common.util.*;

@Entity
@Table(name = "\"user\"")
@Getter @Setter
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "user_name", length = 20)
    private String userName;
    @Column(name = "real_name", length = 30)
    private String realName;
    @Column(name = "password", length = 255)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String password;
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    @Column(name = "email", length = 30, unique = true)
    private String email;
    @Column(name = "birth_date")
    private LocalDate birthDate;
    @Column(name = "gender", length = 1)
    private String gender;
    @Column(name = "open_ranking")
    private Integer openRanking;
    @Column(name = "region_ranking")
    private Integer regionRanking;
    @Column(name = "club_name", length = 50)
    private String clubName;
    @Column(name = "user_type", length = 20)
    private String userType;
    @Column(name = "profile_image", columnDefinition = "text")
    private String profileImage;
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 10)
    private Role role;
}
