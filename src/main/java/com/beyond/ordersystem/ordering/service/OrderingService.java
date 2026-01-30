package com.beyond.ordersystem.ordering.service;

import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.repository.MemberRepository;
import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dtos.OrderCreateDto;
import com.beyond.ordersystem.ordering.dtos.OrderListDto;
import com.beyond.ordersystem.ordering.repository.OrderDetailRepository;
import com.beyond.ordersystem.ordering.repository.OrderingRepository;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    public OrderingService(OrderingRepository orderingRepository, OrderDetailRepository orderDetailRepository, ProductRepository productRepository, MemberRepository memberRepository) {
        this.orderingRepository = orderingRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
    }

    public Long create( List<OrderCreateDto> orderCreateDtoList){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member is not found"));
        Ordering ordering = Ordering.builder()
                .member(member)
                .build();
        for (OrderCreateDto dto : orderCreateDtoList){
            Product product = productRepository.findById(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("entity is not found"));
            OrderDetail orderDetail = OrderDetail.builder()
                    .ordering(ordering)
                    .product(product)
                    .quantity(dto.getProductCount())
                    .build();
            ordering.getOrderDetailList().add(orderDetail);
        }
        orderingRepository.save(ordering);


        return ordering.getId();
    }


    @Transactional(readOnly = true)
    public List<OrderListDto> findAll(){
        return orderingRepository.findAll().stream().map(o->OrderListDto.fromEntity(o)).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<OrderListDto> myorders(){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member is not found"));
        return orderingRepository.findAllByMember(member).stream().map(o->OrderListDto.fromEntity(o)).collect(Collectors.toList());
    }

}
