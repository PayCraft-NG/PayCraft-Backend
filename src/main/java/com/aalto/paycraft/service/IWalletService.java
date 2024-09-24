package com.aalto.paycraft.service;

import com.aalto.paycraft.entity.Employer;
import com.aalto.paycraft.entity.VirtualAccount;

public interface IWalletService {
    VirtualAccount createVirtualAccount(Employer employer);
}
