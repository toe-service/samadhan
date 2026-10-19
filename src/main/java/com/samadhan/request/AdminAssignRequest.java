package com.samadhan.request;

// Body of POST /admin/requests/{requestId}/assign — admin picks a vendor and one of that
// vendor's vehicles; AdminServiceImpl#assignRequest delegates the actual assignment to
// TransferRequestService#requestTransferApproval, the same accept path a vendor's own
// dashboard uses, so it gets the same validation and real wallet deduction for free.
public class AdminAssignRequest {
    private Long vendorId;
    private Long vehicleId;

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }
}
