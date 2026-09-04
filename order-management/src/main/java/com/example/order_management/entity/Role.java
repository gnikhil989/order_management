package com.example.order_management.entity;

/**
 * User Role Enumeration.
 *
 * Defines the access control roles in the application:
 * - USER: Standard customer who can manage their wallet, browse products, and place orders.
 * - ADMIN: Store administrator who can manage inventory, view all orders, and manage users.
 */
public enum Role {
    USER,
    ADMIN
}