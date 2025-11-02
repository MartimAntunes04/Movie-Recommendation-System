package pt.uc.movierecommendation.movierecommendationsystem.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "recommendations")
public class Recommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SQL uses INTEGER for generated_at; mapping as Long (epoch). Consider TIMESTAMP in DB later.
    @Column(name = "generated_at", nullable = false)
    private Long generatedAt;

    @Column(name = "algorithm_type", length = 512)
    private String algorithmType;

    @Column
    private Double score;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "movies_id", nullable = false)
    private Movie movie;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Long generatedAt) { this.generatedAt = generatedAt; }

    public String getAlgorithmType() { return algorithmType; }
    public void setAlgorithmType(String algorithmType) { this.algorithmType = algorithmType; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
