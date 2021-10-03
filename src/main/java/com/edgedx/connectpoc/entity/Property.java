package com.edgedx.connectpoc.entity;

import lombok.*;

import javax.persistence.*;

@Table(name = "properties")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false, length = 500)
    private String scope;

    @Column(nullable = false, length = 500)
    private String propertyKey;

    @Column(nullable = false, length = 500)
    private String propertyValue;

    @Column(nullable = false, length = 500)
    private String propertyName;

}