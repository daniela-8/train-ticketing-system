package domain;

import jakarta.persistence.*;

@Entity
@Table(name = "Trains")
public class Train {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "total_capacity", nullable = false)
    private int totalCapacity;

    // Required by Hibernate
    public Train() {}

    public Train(Long id, String name, int totalCapacity) {
        this.id = id;
        this.name = name;
        this.totalCapacity = totalCapacity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }
}