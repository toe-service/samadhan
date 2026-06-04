package com.samadhan.service;

import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.VendorWallet;

public interface TransferVendorService {

	TransferVendor registerVendor(TransferVendor transferVendor);

	VendorWallet walletByVendor(Long vendorId);

}
