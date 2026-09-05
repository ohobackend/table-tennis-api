package com.tabletennis.app.domain.group;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
@Embeddable @Data @NoArgsConstructor @AllArgsConstructor
public class GroupParticipantId implements Serializable {
    @Column(name = "group_id") private Integer groupId;
    @Column(name = "user_id") private Integer userId;
}
