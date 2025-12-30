// // package com.nguyenviethien.exercise201.repository;

// // import java.util.List;
// // import java.util.UUID;

// // import org.springframework.data.domain.Page;
// // import org.springframework.data.domain.Pageable;
// // import org.springframework.data.jpa.repository.JpaRepository;
// // import org.springframework.data.jpa.repository.Modifying;
// // import org.springframework.data.jpa.repository.Query;
// // import org.springframework.data.repository.query.Param;
// // import org.springframework.stereotype.Repository;

// // import com.nguyenviethien.exercise201.entity.Customer;
// // import com.nguyenviethien.exercise201.entity.Order;
// // import com.nguyenviethien.exercise201.entity.OrderStatus;

// // @Repository
// // public interface OrderRepository extends JpaRepository<Order, String> {

// //     // Các phương thức hiện tại
// //     List<Order> findByCustomer(Customer customer);

// //     List<Order> findByOrderStatus(OrderStatus orderStatus);

// //     List<Order> findByCustomerAndOrderStatus(Customer customer, OrderStatus orderStatus);

// //     // Mở rộng hỗ trợ phân trang

// //     Page<Order> findByCustomer(Customer customer, Pageable pageable);

// //     Page<Order> findByOrderStatus(OrderStatus orderStatus, Pageable pageable);

// //     Page<Order> findByCustomerAndOrderStatus(Customer customer, OrderStatus orderStatus, Pageable pageable);


// //     void deleteAllByCustomer(Customer customer);

// //     List<Order> findAllByCustomer(Customer customer);


// //     // Thêm method để xóa orders theo customer
// //     @Modifying
// //     @Query("DELETE FROM Order o WHERE o.customer = :customer")
// //     void deleteByCustomer(@Param("customer") Customer customer);

// //     // Method để kiểm tra orders theo trạng thái
// //     @Query("SELECT COUNT(o) FROM Order o WHERE o.customer = :customer AND o.orderStatus.statusName IN :statuses")
// //     long countOrdersByCustomerAndStatusNames(@Param("customer") Customer customer, @Param("statuses") List<String> statuses);
// // }



// package com.nguyenviethien.exercise201.repository;

// import java.util.List;
// import java.util.UUID;
// import java.util.Date;

// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Modifying;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// import com.nguyenviethien.exercise201.entity.Customer;
// import com.nguyenviethien.exercise201.entity.Order;
// import com.nguyenviethien.exercise201.entity.OrderStatus;

// @Repository
// public interface OrderRepository extends JpaRepository<Order, String> {

//     // Basic queries
//     List<Order> findByCustomer(Customer customer);
//     List<Order> findByOrderStatus(OrderStatus orderStatus);
//     List<Order> findByCustomerAndOrderStatus(Customer customer, OrderStatus orderStatus);

//     // Enhanced paginated queries
//     Page<Order> findByCustomer(Customer customer, Pageable pageable);
//     Page<Order> findByOrderStatus(OrderStatus orderStatus, Pageable pageable);
//     Page<Order> findByCustomerAndOrderStatus(Customer customer, OrderStatus orderStatus, Pageable pageable);

//     // Search queries for admin
//     @Query("SELECT o FROM Order o WHERE " +
//            "(:search IS NULL OR :search = '' OR " +
//            "LOWER(o.id) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
//            "LOWER(o.customer.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
//            "LOWER(CONCAT(o.customer.first_name, ' ', o.customer.last_name)) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
//            "(:status IS NULL OR :status = '' OR o.orderStatus.statusName = :status)")
//     Page<Order> findOrdersWithFilters(@Param("search") String search, 
//                                     @Param("status") String status, 
//                                     Pageable pageable);

//     // Customer order search
//     @Query("SELECT o FROM Order o WHERE o.customer = :customer AND " +
//            "(:keyword IS NULL OR :keyword = '' OR " +
//            "LOWER(o.id) LIKE LOWER(CONCAT('%', :keyword, '%')))")
//     Page<Order> findByCustomerWithKeyword(@Param("customer") Customer customer, 
//                                         @Param("keyword") String keyword, 
//                                         Pageable pageable);

//     // Date range queries for statistics
//     @Query("SELECT o FROM Order o WHERE o.created_at BETWEEN :startDate AND :endDate")
//     List<Order> findOrdersByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

//     @Query("SELECT o FROM Order o WHERE o.created_at >= :startDate")
//     List<Order> findOrdersAfterDate(@Param("startDate") Date startDate);

//     // Revenue calculations
//     @Query("SELECT SUM(o.totalPrice - COALESCE(o.discountAmount, 0)) FROM Order o WHERE " +
//            "o.orderStatus.statusName NOT IN ('Cancelled') AND " +
//            "(:startDate IS NULL OR o.created_at >= :startDate) AND " +
//            "(:endDate IS NULL OR o.created_at <= :endDate)")
//     Double calculateTotalRevenue(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

//     // Order count by status
//     @Query("SELECT o.orderStatus.statusName, COUNT(o) FROM Order o GROUP BY o.orderStatus.statusName")
//     List<Object[]> getOrderCountByStatus();

//     @Query("SELECT o.orderStatus.statusName, COUNT(o) FROM Order o WHERE " +
//            "(:startDate IS NULL OR o.created_at >= :startDate) AND " +
//            "(:endDate IS NULL OR o.created_at <= :endDate) " +
//            "GROUP BY o.orderStatus.statusName")
//     List<Object[]> getOrderCountByStatusAndDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

//     // Customer specific queries
//     @Query("SELECT COUNT(o) FROM Order o WHERE o.customer = :customer AND o.orderStatus.statusName IN :statuses")
//     long countOrdersByCustomerAndStatusNames(@Param("customer") Customer customer, @Param("statuses") List<String> statuses);

//     @Query("SELECT o FROM Order o WHERE o.customer = :customer AND o.orderStatus.statusName = :status")
//     Page<Order> findByCustomerAndStatusName(@Param("customer") Customer customer, 
//                                           @Param("status") String status, 
//                                           Pageable pageable);

//     // Delete operations
//     void deleteAllByCustomer(Customer customer);
//     List<Order> findAllByCustomer(Customer customer);

//     @Modifying
//     @Query("DELETE FROM Order o WHERE o.customer = :customer")
//     void deleteByCustomer(@Param("customer") Customer customer);

//     // Recent orders
//     @Query("SELECT o FROM Order o WHERE o.customer = :customer ORDER BY o.created_at DESC")
//     List<Order> findRecentOrdersByCustomer(@Param("customer") Customer customer, Pageable pageable);

//     // Orders pending approval
//     @Query("SELECT o FROM Order o WHERE o.orderStatus.statusName = 'Pending' ORDER BY o.created_at ASC")
//     List<Order> findPendingOrders();

//     @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus.statusName = 'Pending'")
//     long countPendingOrders();

//     // Orders ready to ship
//     @Query("SELECT o FROM Order o WHERE o.orderStatus.statusName = 'Approved' AND o.orderApprovedAt IS NOT NULL ORDER BY o.orderApprovedAt ASC")
//     List<Order> findOrdersReadyToShip();

//     // Monthly/yearly statistics
//     @Query("SELECT YEAR(o.created_at), MONTH(o.created_at), COUNT(o), SUM(o.totalPrice - COALESCE(o.discountAmount, 0)) " +
//            "FROM Order o WHERE o.orderStatus.statusName NOT IN ('Cancelled') " +
//            "GROUP BY YEAR(o.created_at), MONTH(o.created_at) " +
//            "ORDER BY YEAR(o.created_at) DESC, MONTH(o.created_at) DESC")
//     List<Object[]> getMonthlyOrderStatistics();


// }





package com.nguyenviethien.exercise201.repository;

import java.util.List;
import java.util.UUID;
import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nguyenviethien.exercise201.entity.Customer;
import com.nguyenviethien.exercise201.entity.Order;
import com.nguyenviethien.exercise201.entity.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    // Basic queries
    List<Order> findByCustomer(Customer customer);
    List<Order> findByOrderStatus(OrderStatus orderStatus);
    List<Order> findByCustomerAndOrderStatus(Customer customer, OrderStatus orderStatus);

    // FIXED: Enhanced paginated queries with explicit ORDER BY to avoid field name issues
    @Query("SELECT o FROM Order o WHERE o.customer = :customer ORDER BY o.created_at DESC")
    Page<Order> findByCustomer(@Param("customer") Customer customer, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.orderStatus = :orderStatus ORDER BY o.created_at DESC")
    Page<Order> findByOrderStatus(@Param("orderStatus") OrderStatus orderStatus, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.customer = :customer AND o.orderStatus = :orderStatus ORDER BY o.created_at DESC")
    Page<Order> findByCustomerAndOrderStatus(@Param("customer") Customer customer, @Param("orderStatus") OrderStatus orderStatus, Pageable pageable);

    // Search queries for admin
    @Query("SELECT o FROM Order o WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(o.id) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.customer.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CONCAT(o.customer.first_name, ' ', o.customer.last_name)) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR o.orderStatus.statusName = :status) " +
           "ORDER BY o.created_at DESC")
    Page<Order> findOrdersWithFilters(@Param("search") String search, 
                                    @Param("status") String status, 
                                    Pageable pageable);

    // Customer order search
    @Query("SELECT o FROM Order o WHERE o.customer = :customer AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(o.id) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY o.created_at DESC")
    Page<Order> findByCustomerWithKeyword(@Param("customer") Customer customer, 
                                        @Param("keyword") String keyword, 
                                        Pageable pageable);

    // Date range queries for statistics
    @Query("SELECT o FROM Order o WHERE o.created_at BETWEEN :startDate AND :endDate ORDER BY o.created_at DESC")
    List<Order> findOrdersByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT o FROM Order o WHERE o.created_at >= :startDate ORDER BY o.created_at DESC")
    List<Order> findOrdersAfterDate(@Param("startDate") Date startDate);

    // Revenue calculations
    @Query("SELECT SUM(o.totalPrice - COALESCE(o.discountAmount, 0)) FROM Order o WHERE " +
           "o.orderStatus.statusName NOT IN ('Cancelled') AND " +
           "(:startDate IS NULL OR o.created_at >= :startDate) AND " +
           "(:endDate IS NULL OR o.created_at <= :endDate)")
    Double calculateTotalRevenue(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    // Order count by status
    @Query("SELECT o.orderStatus.statusName, COUNT(o) FROM Order o GROUP BY o.orderStatus.statusName")
    List<Object[]> getOrderCountByStatus();

    @Query("SELECT o.orderStatus.statusName, COUNT(o) FROM Order o WHERE " +
           "(:startDate IS NULL OR o.created_at >= :startDate) AND " +
           "(:endDate IS NULL OR o.created_at <= :endDate) " +
           "GROUP BY o.orderStatus.statusName")
    List<Object[]> getOrderCountByStatusAndDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    // Customer specific queries
    @Query("SELECT COUNT(o) FROM Order o WHERE o.customer = :customer AND o.orderStatus.statusName IN :statuses")
    long countOrdersByCustomerAndStatusNames(@Param("customer") Customer customer, @Param("statuses") List<String> statuses);

    @Query("SELECT o FROM Order o WHERE o.customer = :customer AND o.orderStatus.statusName = :status ORDER BY o.created_at DESC")
    Page<Order> findByCustomerAndStatusName(@Param("customer") Customer customer, 
                                          @Param("status") String status, 
                                          Pageable pageable);

    // Delete operations
    void deleteAllByCustomer(Customer customer);
    List<Order> findAllByCustomer(Customer customer);

    @Modifying
    @Query("DELETE FROM Order o WHERE o.customer = :customer")
    void deleteByCustomer(@Param("customer") Customer customer);

    // Recent orders
    @Query("SELECT o FROM Order o WHERE o.customer = :customer ORDER BY o.created_at DESC")
    List<Order> findRecentOrdersByCustomer(@Param("customer") Customer customer, Pageable pageable);

    // Orders pending approval
    @Query("SELECT o FROM Order o WHERE o.orderStatus.statusName = 'Pending' ORDER BY o.created_at ASC")
    List<Order> findPendingOrders();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus.statusName = 'Pending'")
    long countPendingOrders();

    // Orders ready to ship
    @Query("SELECT o FROM Order o WHERE o.orderStatus.statusName = 'Approved' AND o.orderApprovedAt IS NOT NULL ORDER BY o.orderApprovedAt ASC")
    List<Order> findOrdersReadyToShip();

    // Monthly/yearly statistics
    @Query("SELECT YEAR(o.created_at), MONTH(o.created_at), COUNT(o), SUM(o.totalPrice - COALESCE(o.discountAmount, 0)) " +
           "FROM Order o WHERE o.orderStatus.statusName NOT IN ('Cancelled') " +
           "GROUP BY YEAR(o.created_at), MONTH(o.created_at) " +
           "ORDER BY YEAR(o.created_at) DESC, MONTH(o.created_at) DESC")
    List<Object[]> getMonthlyOrderStatistics();
}






