package com.samadhan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;

@Entity
@Table(name="Service_centre")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class VehicleTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}
