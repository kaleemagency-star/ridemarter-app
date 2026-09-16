package com.example

import com.ridemarter.app.model.UserData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DriverApprovalTest {

    @Test
    fun defaultUserData_isPendingAndNotApproved() {
        val user = UserData(
            uid = "test-uid-123",
            userId = "SD12345678",
            name = "John Doe",
            email = "john@example.com",
            mobile = "9876543210",
            vehicleType = "AUTO"
        )

        assertFalse("New driver must not be approved by default", user.approved)
        assertEquals("pending", user.status)
        assertEquals("pending", user.approvalStatus)
        assertEquals("", user.rejectionReason)
    }

    @Test
    fun approvedUserData_hasApprovedStatus() {
        val user = UserData(
            uid = "test-uid-123",
            userId = "SD12345678",
            name = "John Doe",
            email = "john@example.com",
            mobile = "9876543210",
            vehicleType = "AUTO",
            approved = true,
            status = "approved",
            approvalStatus = "approved"
        )

        assertTrue(user.approved)
        assertEquals("approved", user.status)
        assertEquals("approved", user.approvalStatus)
    }

    @Test
    fun rejectedUserData_containsRejectionReason() {
        val user = UserData(
            uid = "test-uid-456",
            userId = "SD87654321",
            name = "Jane Smith",
            email = "jane@example.com",
            mobile = "9876543211",
            vehicleType = "CAB",
            approved = false,
            status = "rejected",
            approvalStatus = "rejected",
            rejectionReason = "Duplicate mobile number detected"
        )

        assertFalse(user.approved)
        assertEquals("rejected", user.status)
        assertEquals("Duplicate mobile number detected", user.rejectionReason)
    }
}
