package com.customermanager.util;

import com.customermanager.model.Customer;
import java.util.List;

/**
 * Matches the Spring Page<Customer> JSON structure:
 * { "content": [...], "totalElements": N, "totalPages": N, ... }
 */
public class PagedResponse {
    private List<Customer> content;
    private int totalElements;
    private int totalPages;
    private int number;
    private int size;

    public List<Customer> getContent()    { return content; }
    public int getTotalElements()         { return totalElements; }
    public int getTotalPages()            { return totalPages; }
    public int getNumber()                { return number; }
    public int getSize()                  { return size; }
}
