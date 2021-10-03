package com.edgedx.connectpoc.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MessageNotification {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "trigger_name", length = 250)
    private String triggerName;

    @Column(name = "text", length = 500)
    @NotEmpty(message = "This field is required")
    private String text;


}