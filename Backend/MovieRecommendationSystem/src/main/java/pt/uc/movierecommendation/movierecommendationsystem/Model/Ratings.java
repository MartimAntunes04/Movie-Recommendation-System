package pt.uc.movierecommendation.movierecommendationsystem.Model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "ratings")
public class Ratings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rate", nullable = false)
    private Float rating;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "movies_id", nullable = false)
    private Movie movie;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Float getRating() { return this.rating; }
    public void setRating(Float rating) { this.rating = rating; }

    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
