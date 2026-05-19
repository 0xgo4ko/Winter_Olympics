package wo.org.winter_olympics.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Column;
import wo.org.winter_olympics.data.common.BaseEntityModel;

import java.math.BigDecimal;

@Entity
@Table(
        name = "competition_registrations",
        uniqueConstraints = @UniqueConstraint(name = "uk_competition_registrations_user", columnNames = "user_id")
)
public class CompetitionRegistrationEntity extends BaseEntityModel {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competition_id", nullable = false)
    private CompetitionEntity competition;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUserEntity user;

    @Column(name = "did_not_finish", nullable = false)
    private boolean didNotFinish;

    @Column(name = "qualified_for_second_run", nullable = false)
    private boolean qualifiedForSecondRun;

    @Column(name = "first_run_time", precision = 10, scale = 3)
    private BigDecimal firstRunTime;

    @Column(name = "second_run_did_not_finish", nullable = false)
    private boolean secondRunDidNotFinish;

    @Column(name = "second_run_time", precision = 10, scale = 3)
    private BigDecimal secondRunTime;

    @Column(name = "biathlon_time", precision = 10, scale = 3)
    private BigDecimal biathlonTime;

    @Column(name = "missed_targets")
    private Integer missedTargets;

    public CompetitionEntity getCompetition() {
        return competition;
    }

    public void setCompetition(CompetitionEntity competition) {
        this.competition = competition;
    }

    public AppUserEntity getUser() {
        return user;
    }

    public void setUser(AppUserEntity user) {
        this.user = user;
    }

    public boolean isDidNotFinish() {
        return didNotFinish;
    }

    public void setDidNotFinish(boolean didNotFinish) {
        this.didNotFinish = didNotFinish;
    }

    public boolean isQualifiedForSecondRun() {
        return qualifiedForSecondRun;
    }

    public void setQualifiedForSecondRun(boolean qualifiedForSecondRun) {
        this.qualifiedForSecondRun = qualifiedForSecondRun;
    }

    public BigDecimal getFirstRunTime() {
        return firstRunTime;
    }

    public void setFirstRunTime(BigDecimal firstRunTime) {
        this.firstRunTime = firstRunTime;
    }

    public boolean isSecondRunDidNotFinish() {
        return secondRunDidNotFinish;
    }

    public void setSecondRunDidNotFinish(boolean secondRunDidNotFinish) {
        this.secondRunDidNotFinish = secondRunDidNotFinish;
    }

    public BigDecimal getSecondRunTime() {
        return secondRunTime;
    }

    public void setSecondRunTime(BigDecimal secondRunTime) {
        this.secondRunTime = secondRunTime;
    }

    public BigDecimal getBiathlonTime() {
        return biathlonTime;
    }

    public void setBiathlonTime(BigDecimal biathlonTime) {
        this.biathlonTime = biathlonTime;
    }

    public Integer getMissedTargets() {
        return missedTargets;
    }

    public void setMissedTargets(Integer missedTargets) {
        this.missedTargets = missedTargets;
    }
}
