package com.sparta.sportify.service.reservation;

import com.sparta.sportify.dto.reservation.request.ReservationRequestDto;
import com.sparta.sportify.entity.StadiumTime.StadiumTime;
import com.sparta.sportify.entity.stadium.Stadium;
import com.sparta.sportify.entity.stadium.StadiumStatus;
import com.sparta.sportify.entity.team.Team;
import com.sparta.sportify.entity.team.TeamColor;
import com.sparta.sportify.entity.teamMember.TeamMember;
import com.sparta.sportify.entity.teamMember.TeamMemberRole;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.repository.*;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Transactional
@SpringBootTest
public class ReservationConcurrencyTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StadiumTimeRepository stadiumTimeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private StadiumRepository stadiumRepository;

    List<User> listUser = new ArrayList<>();
    Stadium stadium;
    StadiumTime stadiumTime;
    List<ReservationRequestDto> listRequestDto = new ArrayList<>();
    Team team;
    List<TeamMember> teamMembers = new ArrayList<>();

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    void dataSetUp() {
        for (int i = 0; i < 30; i++) {
            listUser.add(userRepository.save(User.builder().
                    email("testerspring" + i + "@test.com")
                    .age(20L)
                    .cash(30000L)
                    .name("이름" + i)
                    .gender("남성")
                    .levelPoints(1000L)
                    .password("test1234!")
                    .role(UserRole.USER)
                    .region("지역지역")
                    .build()
            ));
        }

        stadium = stadiumRepository.save(Stadium.builder()
                .price(2000L)
                .aTeamCount(6)
                .bTeamCount(6)
                .status(StadiumStatus.APPROVED)
                .user(listUser.get(0))
                .build()
        );

        stadiumTime = stadiumTimeRepository.saveAndFlush(StadiumTime.builder()
                .stadium(stadium)
                .cron("0 0 10-12,12-14 ? * MON,TUE,WED,THU,FRI,SAT,SUN")
                .build()
        );
//        유저1-10까지 팀이라 가정
        Team team = teamRepository.save(Team.builder()
                .region("경기")
                .activityTime("16-20")
                .teamName("테스트팀")
                .description("설명")
                .skillLevel("고수")
                .sportType("축구")
                .teamPoints(2000)
                .build());

        for (int i = 0; i < 10; i++) {
            teamMembers.add(teamMemberRepository.save(TeamMember.builder()
                    .user(listUser.get(0))
                    .team(team)
                    .teamMemberRole(i==0 ? TeamMemberRole.TEAM_OWNER : TeamMemberRole.USER)
                    .status(TeamMember.Status.APPROVED)
                    .build()));
        }

        for (int i = 0; i < 30; i++) {
            listRequestDto.add(new ReservationRequestDto());
            listRequestDto.get(i).setStadiumTimeId(stadiumTime.getId());
            listRequestDto.get(i).setReservationDate(LocalDate.of(2024, 12, 3));
            listRequestDto.get(i).setTime(10);
            listRequestDto.get(i).setTeamColor(i < 15 ? TeamColor.A :TeamColor.B);
        }


    }

        @Test
        void reservationConcurrencyPersonal() throws InterruptedException {
        final int numberOfThreads = 30;

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        CyclicBarrier barrier = new CyclicBarrier(numberOfThreads);

        executorService.submit(() -> {
            dataSetUp();
        });

        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    barrier.await();
                    UserDetailsImpl authUser = new UserDetailsImpl(listUser.get(index).getEmail(),UserRole.USER,listUser.get(index));
                    reservationService.reservationPersonal(listRequestDto.get(index),authUser);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);


    }
}
