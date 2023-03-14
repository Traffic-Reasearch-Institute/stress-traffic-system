package com.project.stress_traffic_system.order.service;

import com.project.stress_traffic_system.cart.model.Cart;
import com.project.stress_traffic_system.cart.model.CartItem;
import com.project.stress_traffic_system.cart.repository.CartItemRepository;
import com.project.stress_traffic_system.cart.repository.CartRepository;
import com.project.stress_traffic_system.cart.service.CartService;
import com.project.stress_traffic_system.facade.RedissonLockStockFacade;
import com.project.stress_traffic_system.members.entity.Members;
import com.project.stress_traffic_system.members.entity.MembersRoleEnum;
import com.project.stress_traffic_system.members.repository.MembersRepository;
import com.project.stress_traffic_system.order.model.OrderItem;
import com.project.stress_traffic_system.order.model.Orders;
import com.project.stress_traffic_system.order.model.dto.OrderDetailDto;
import com.project.stress_traffic_system.order.model.dto.OrderDto;
import com.project.stress_traffic_system.order.model.dto.OrderListDto;
import com.project.stress_traffic_system.order.model.dto.OrderRequestDto;
import com.project.stress_traffic_system.order.repository.OrderRepository;
import com.project.stress_traffic_system.product.model.Product;
import com.project.stress_traffic_system.product.model.dto.ProductResponseDto;
import com.project.stress_traffic_system.product.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("orderService 테스트")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RedisCacheManager redisCacheManager;

    @Mock
    private RedisTemplate<String, ProductResponseDto> productRedisTemplate;

    @Autowired private OrderService orderService;

    Members member;
    Cart cart;

    Product product;
    CartItem cartItem;
    OrderRequestDto orderRequestDto;

    @BeforeEach
    void beforeEach() {
        member = new Members(1L,"user", "1234", "test@test.com", "test", MembersRoleEnum.MEMBER);
        cart = new Cart(member);
        product = new Product(1L, 30, "testName", 16000, 50,0L);

        cartItem = new CartItem(cart, product);

        orderRequestDto = new OrderRequestDto();
        orderRequestDto.setProductId(2L);
        orderRequestDto.setQuantity(10);
        orderRequestDto.setDiscount(1000F);
        orderRequestDto.setDcType("none");
    }



    @Nested
    @DisplayName("성공 케이스")
    class successCase {

        @Test
        @DisplayName("단일 상품 주문")
        void orderOne() {
            //given

            //orderRepository.save()메서드가 호출되면 가로채서 save()의 0번째 인자를 리턴하도록 함.
            when(orderRepository.save(any(Orders.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ValueOperations<String, ProductResponseDto> productValueOperations = mock(ValueOperations.class);
            when(productRedisTemplate.opsForValue()).thenReturn(productValueOperations);

            //회원의 장바구니를 찾을 때 내가 미리 만들어둔 cart를 리턴해주기 위해
            Map<Members, Cart> testCartMap = new HashMap<>();
            testCartMap.put(member, cart);
            OrderService orderService = new OrderService(orderRepository, productRepository, cartItemRepository, cartRepository, redissonClient, redisCacheManager,productRedisTemplate) {

                //findCart라는 메서드가 사용되면 내가 지정한 cart를 바로 가져오게 하기 위해
                //그냥 원래 service에 있던 코드를 그대로 가져와도 되는데, 그러면 when으로 가로채야 하는 코드가 더 생겨서 이렇게 작성함
                @Override
                protected Cart findCart(Members members) {
                    return testCartMap.get(member);
                }

                @Override
                protected Product checkProduct(OrderRequestDto requestDto) {
                    return product;
                }

                @Override
                public ProductResponseDto findProductInCache(Long productid, String productName){
                    return new ProductResponseDto(product);
                }
                //service에 있는 대로 그대로 복사. override를 안하면 mock객체를 보는 게 아니어서 null값을 리턴함.
                @Override
                protected void checkStock(OrderRequestDto orderRequestDto, Product product) {
                    if (orderRequestDto == null) {
                        throw new IllegalArgumentException("주문 정보가 존재하지 않습니다.");
                    }
                    if (product == null) {
                        throw new IllegalArgumentException("상품 정보가 존재하지 않습니다.");
                    }
                    if (orderRequestDto.getQuantity() > product.getStock()) {
                        throw new IllegalArgumentException("주문 가능 수량을 초과하였습니다");
                    }
                }

                @Override
                protected void checkQuantity(OrderRequestDto requestDto) {
                    if (requestDto.getQuantity() < 1) {
                        throw new IllegalArgumentException("최소 1개 이상 주문해주세요");
                    }
                }
            };

            //when
            orderService.orderOne(member, orderRequestDto);

            //then
            //재고가 차감되었는지 확인
            Assertions.assertEquals(30, product.getStock());
            //장바구니가 다 지워졌는지
            Assertions.assertEquals(0,cartItemRepository.findByCartAndProduct(cart,product).stream().count());



        }

        @Test
        @DisplayName("주문내역 리스트 가져오기")
        void getOrders() {
            //given

            //회원의 주문 리스트 생성
            List<Orders> ordersList = new ArrayList<>();

            //findAllByMembersOrderByCreatedAtAsc 호출되면 미리 생성한 주문 리스트 반환
            when(orderRepository.findAllByMembersOrderByCreatedAtAsc(member)).thenReturn(ordersList);
            OrderService orderService = new OrderService(orderRepository, productRepository, cartItemRepository, cartRepository
                    , redissonClient, redisCacheManager,productRedisTemplate);

            //when
            List<OrderListDto> orders = orderService.getOrders(member);

            //then
            //두 코드가 어짜피 같은 의미인 것 같아 하나는 주석처리. 테스트 내용 더 생각해보고 없으면 지우기
            //미리 생성한 주문 리스트가 비어있으니, 비어있는 지 확인
            //Assertions.assertEquals(0,orders.size());
            Assertions.assertTrue(orders.isEmpty());
        }

        @Test
        @DisplayName("주문 상세내역 가져오기")
        void getOrderDetail() {


            //given
            List<OrderItem> orderItems = new ArrayList<>(); //orderItem들을 담아둘 리스트 생성

            //두 번 주문하기
            OrderItem orderItem = OrderItem.createOrderItem(product, orderRequestDto);
            OrderItem orderItem2 = OrderItem.createOrderItem(product, orderRequestDto);
            orderItems.add(orderItem);
            orderItems.add(orderItem2);

            Orders order = new Orders(1L, member, orderItems); //member가 주문한 orderItem들이 들어간 order생성

            OrderService orderService = new OrderService(orderRepository, productRepository, cartItemRepository, cartRepository
                    , redissonClient, redisCacheManager,productRedisTemplate);
            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

            //when
            List<OrderDetailDto> orderDetail = orderService.getOrderDetail(member, 1L);

            //then
            Assertions.assertEquals(1L, orderDetail.get(0).getOrderId()); //해당 메서드를 통해 생성된 orderDetail의 첫번째 주문 제품의 id와 내가 처음에 넣어둔 id가 같은지 비교
            Assertions.assertEquals(20, orderDetail.get(0).getQuantity() + orderDetail.get(1).getQuantity()); //각 주문 수량들의 합이 내가 설정한 주문들의 수량들의 합과 같은지 비교


        }

        @Test
        @DisplayName("상품이 존재하는지 확인 - pessimistic lock")
        void checkProductWithPessimisticLock() {
            //given
            when(productRepository.findByIdWithPessimisticLock(orderRequestDto.getProductId())).thenReturn(product);
            OrderService orderService = new OrderService(orderRepository, productRepository, cartItemRepository, cartRepository
                    , redissonClient, redisCacheManager,productRedisTemplate);
            //when
            Product findResult = orderService.checkProductWithPessimisticLock(orderRequestDto);
            //then
            Assertions.assertEquals(product, findResult);
        }

        @Test
        @DisplayName("상품이 존재하는지 확인")
        void checkProductTest() {
            //given
            when(productRepository.findById(orderRequestDto.getProductId())).thenReturn(Optional.ofNullable(product));
            OrderService orderService = new OrderService(orderRepository, productRepository, cartItemRepository, cartRepository
                    , redissonClient, redisCacheManager,productRedisTemplate);
            //when
            Product findResult = orderService.checkProduct(orderRequestDto);
            //then
            Assertions.assertEquals(product, findResult);

        }

        @Test
        @DisplayName("checkStock")
        void checkStockTest() {
            //given
            OrderService orderService = new OrderService(orderRepository, productRepository, cartItemRepository, cartRepository
                    , redissonClient, redisCacheManager,productRedisTemplate);
            //when

            //then
            Assertions.assertDoesNotThrow(() -> orderService.checkStock(orderRequestDto, product));
        }
    }


    @Nested
    @DisplayName("실패 케이스")
    class failCase {
        @Test
        @DisplayName("checkStock - orderRequestDto가 null일 때")
        void checkStockFail1() {
            //given
            OrderService orderService = new OrderService(orderRepository, productRepository, cartItemRepository, cartRepository
                    , redissonClient, redisCacheManager,productRedisTemplate);
            OrderRequestDto orderRequestDto = null;
            //when

            //then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.checkStock(orderRequestDto, product));
            assertEquals("주문 정보가 존재하지 않습니다.", exception.getMessage());
        }

        @Test
        @DisplayName("checkStock - product가 null일 때")
        void checkStockFail2() {
            //given
            OrderService orderService = new OrderService(orderRepository, productRepository, cartItemRepository, cartRepository
                    , redissonClient, redisCacheManager,productRedisTemplate);
            Product product = null;
            //when

            //then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.checkStock(orderRequestDto, product));
            assertEquals("상품 정보가 존재하지 않습니다.", exception.getMessage());
        }

        @Test
        @DisplayName("checkStock - 주문 수량이 재고보다 많을 때")
        void checkStockFail() {
            //given
            OrderService orderService = new OrderService(orderRepository, productRepository, cartItemRepository, cartRepository
                    ,redissonClient, redisCacheManager,productRedisTemplate);
            orderRequestDto.setQuantity(100);
            //when

            //then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.checkStock(orderRequestDto, product));
            assertEquals("주문 가능 수량을 초과하였습니다", exception.getMessage());
        }

        @Test
        @DisplayName("여러상품 주문하기 - redissonLock")
        @Transactional
        void orderManyWithRedissonLock() {
            //given
            List<OrderRequestDto> orderRequestDtos = new ArrayList<>();
            orderRequestDtos.add(orderRequestDto);

            //when
            OrderDto orderDto = orderService.orderManyWithRedissonLock(member,orderRequestDtos);

            //then
            assertNotNull(orderDto);
        }

        @Test
        @DisplayName("여러상품 주문하기")
        @Transactional
        void orderMany() {
            //given
            List<OrderRequestDto> orderRequestDtos = new ArrayList<>();
            orderRequestDtos.add(orderRequestDto);

            //when
            OrderDto orderDto = orderService.orderMany(member,orderRequestDtos);

            //then
            assertNotNull(orderDto);
        }

        @Test
        @DisplayName("여러상품 주문하기 - PessimisticLock")
        @Transactional
        void orderManyWithPessimisticLock() {
            //given
            List<OrderRequestDto> orderRequestDtos = new ArrayList<>();
            orderRequestDtos.add(orderRequestDto);

            //when
            OrderDto orderDto = orderService.orderManyWithPessimisticLock(member,orderRequestDtos);

            //then
            assertNotNull(orderDto);
        }

        @Test
        @DisplayName("캐시상품 찾기 (캐시미스)")
        @Transactional
        void findProductInCache() {
            //given
            List<OrderRequestDto> orderRequestDtos = new ArrayList<>();
            orderRequestDtos.add(orderRequestDto);

            //when
            ProductResponseDto responseDto = orderService.findProductInCache(1L,"test123");

            //then
            assertEquals(responseDto.getName(),"test");
        }

        @Test
        @DisplayName("캐시저장하기")
        @Transactional
        void saveProductInCache() {
            //given
            ProductResponseDto productDto = ProductResponseDto.builder().id(1L).imgurl(20).name("Robbie").build();

            //when
            ProductResponseDto responseDto = orderService.saveProductInCache(productDto);

            //then
            assertEquals(responseDto.getName(),productDto.getName());
        }

    }

}
