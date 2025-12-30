package com.nguyenviethien.exercise201.DTO;

import java.util.List;
import com.nguyenviethien.exercise201.entity.Customer;

public class CustomerPageResponse {
    private List<Customer> customers;
    private PageMetadata pageMetadata;

    public CustomerPageResponse(List<Customer> customers, PageMetadata pageMetadata) {
        this.customers = customers;
        this.pageMetadata = pageMetadata;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public PageMetadata getPageMetadata() {
        return pageMetadata;
    }

    public static class PageMetadata {
        private int size;
        private long totalElements;
        private int totalPages;
        private int number;

        public PageMetadata(int size, long totalElements, int totalPages, int number) {
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.number = number;
        }

        public int getSize() {
            return size;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public int getNumber() {
            return number;
        }
    }
}
