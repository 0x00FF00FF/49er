package org.rares.miner49er.persistence.dao.converters;

import java.util.List;

public interface DaoConverter<DM, VM> {

    DM vmToDm(VM viewModel);
    VM dmToVm(DM databaseModel);

    List<DM> vmToDm(List<VM> viewModelList);
    List<VM> dmToVm(List<DM> databaseModelList);
}
