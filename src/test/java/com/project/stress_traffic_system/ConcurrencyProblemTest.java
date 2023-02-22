//package com.project.stress_traffic_system;
//
//import com.project.stress_traffic_system.cart.repository.CartRepository;
//import com.project.stress_traffic_system.cart.service.CartService;
//import com.project.stress_traffic_system.facade.RedissonLockStockFacade;
//import com.project.stress_traffic_system.order.service.OrderService;
//import com.project.stress_traffic_system.product.model.Product;
//import com.project.stress_traffic_system.product.repository.ProductRepository;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//@SpringBootTest
//@Slf4j
//@Nested
//
//
//@DisplayName("동시성 이슈 테스트")
//class ConcurrencyProblemTest {
//
//
//    @Autowired
//    private ProductRepository productRepository;
//    @Autowired
//    private CartRepository cartRepository;
//    @Autowired
//    private CartService cartService;
//
//    @Autowired
//    private OrderService orderService;
//
//    @Autowired
//    private RedissonLockStockFacade redissonLockStockFacade;
//
//    @BeforeEach
//    public void before() {
//        Product product = new Product(1L, 100,"test",12000,1);
//        productRepository.saveAndFlush(product);
//    }
//
////    @AfterEach
////    public void after() {
////        productRepository.deleteAll();
////    }
//
//    @Nested
//    @DisplayName("성공 케이스")
//
//    class successCase {
//        @Test
//        @DisplayName("동시성 제어 - redisson (pub-sub기반)")
//        void decreaseTest_redisson() throws InterruptedException {
//            int threadCount = 100;
//
//            //스레드 테스트를 진행할 때 아래 코드를 사용합니다.
//            //스레드 풀 설정과 관련해서는 깊게 생각하지 않았습니다. 적정 스레드풀 설정과 관련해서는 자료들이 있긴 했으나, 지금 단계에서 볼 것은 아니라고 판단했습니다.
//            ExecutorService executorService = Executors.newFixedThreadPool(30);
//            //스레드 100개가 작업을 완료할때까지 대기
//            //countDownLatch는 멀티스레드 환경에서 작업의 시작, 완료 신호를 보내는데 사용됨
//            CountDownLatch latch = new CountDownLatch(threadCount);
//            //스레드풀에서 작업 시작
//            //스레드 하나가 작업 끝내면 countDown으로 작업 끝냈다고 알림
//            for (int i = 0; i < threadCount; i++) {
//                executorService.submit(
//                        () ->  {
//                            try {
//                                redissonLockStockFacade.decrease(1L, 1);
//                            } finally {
//                                latch.countDown();
//                            }
//                        }
//                );
//            }
//
//            latch.await();
//            Product product = productRepository.findById(1L).orElseThrow();
//            Assertions.assertEquals(0, product.getStock());
//        }
//        @Test
//        @DisplayName("동시성 제어 - pessimistic lock")
//        void decreaseTest_pessimisticLock() throws InterruptedException {
//
//            int threadCount = 100;
//            ExecutorService executorService = Executors.newFixedThreadPool(30);
//
//            //스레드 100개가 작업을 완료할때까지 대기
//            //countDownLatch는 멀티스레드 환경에서 작업의 시작, 완료 신호를 보내는데 사용됨
//            CountDownLatch latch = new CountDownLatch(threadCount);
//
//            //스레드풀에서 작업 시작
//            //스레드 하나가 작업 끝내면 countDown으로 작업 끝냈다고 알림
//            for (int i = 0; i < threadCount; i++) {
//                executorService.submit(
//                        () ->  {
//                            try {
//                                orderService.decrease(1L,1);
//                            }finally {
//                                latch.countDown();
//                            }
//                        }
//                );
//            }
//
//            //모든 작업이 끝날때까지 기다림
//            //countDownLatch가 0이 되기 전에 끝나지 않도록 함.
//            //스레드 풀에서 아직 작업 중인데 for 문 끝나고 재고 조회로 넘어가지 않도록
//            latch.await();
//            Product product = productRepository.findById(1L).orElseThrow();
//            Assertions.assertEquals(0, product.getStock());
//        }
//    }
//
//    @Nested
//    @DisplayName("실패 케이스")
//    class failCase{
//        @Test
//        @DisplayName("동시성 제어 - locking 설정하지 않았을 때")
//        void decrease() throws InterruptedException {
//            int threadCount = 100;
//            ExecutorService executorService = Executors.newFixedThreadPool(30);
//
//            //스레드 100개가 작업을 완료할때까지 대기
//            //countDownLatch는 멀티스레드 환경에서 작업의 시작, 완료 신호를 보내는데 사용됨
//            CountDownLatch latch = new CountDownLatch(threadCount);
//
//            //스레드풀에서 작업 시작
//            //스레드 하나가 작업 끝내면 countDown으로 작업 끝냈다고 알림
//            for (int i = 0; i < threadCount; i++) {
//                executorService.submit(
//                        () ->  {
//                            try {
//                                Product product = productRepository.findById(1L).orElseThrow();
//                                product.removeStock(1);
//                                productRepository.save(product);;
//                            }finally {
//                                latch.countDown();
//                            }
//                        }
//                );
//            }
//
//            //모든 작업이 끝날때까지 기다림
//            //countDownLatch가 0이 되기 전에 끝나지 않도록 함.
//            //스레드 풀에서 아직 작업 중인데 for 문 끝나고 재고 조회로 넘어가지 않도록
//            latch.await();
//            Product product = productRepository.findById(1L).orElseThrow();
//            Assertions.assertNotEquals(0, product.getStock());
//        }
//    }
//}
