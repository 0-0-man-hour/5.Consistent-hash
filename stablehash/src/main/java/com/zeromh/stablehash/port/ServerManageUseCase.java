package com.zeromh.stablehash.port;

import com.zeromh.stablehash.domain.StableHashServer;

public interface ServerManageUseCase {
    Boolean addServer(StableHashServer stableHashServer);
    Boolean deleteServer(StableHashServer stableHashServer);
}
