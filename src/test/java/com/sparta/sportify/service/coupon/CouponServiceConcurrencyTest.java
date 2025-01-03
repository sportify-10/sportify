package com.sparta.sportify.service.coupon;

import com.sparta.sportify.entity.coupon.Coupon;
import com.sparta.sportify.entity.coupon.CouponStatus;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.repository.CouponRepository;
import com.sparta.sportify.repository.UserRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.CouponService;
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
public class CouponServiceConcurrencyTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponService couponService;

    List<User> listUser = new ArrayList<>();

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

        couponRepository.save(Coupon.builder()
                .price(2000L)
                .count(20L)
                .status(CouponStatus.AVAILABLE)
                .code("NEWYEAR2025")
                .expireDate(LocalDate.of(2025, 01, 01))
                .name("새해기념")
                .build());

    }

    @Test
    void couponUseTest() throws InterruptedException {
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
                    UserDetailsImpl authUser = new UserDetailsImpl(listUser.get(index).getEmail(), UserRole.USER,listUser.get(index));
                    couponService.useCoupon("NEWYEAR2025",authUser);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

    }
}
