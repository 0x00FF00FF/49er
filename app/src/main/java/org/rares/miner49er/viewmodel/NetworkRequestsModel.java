package org.rares.miner49er.viewmodel;

import androidx.lifecycle.ViewModel;
import lombok.Getter;
import org.rares.miner49er.cache.ViewModelCache;
import org.rares.miner49er.cache.ViewModelCacheSingleton;
import org.rares.miner49er.network.DataUpdater;
import org.rares.miner49er.network.ObservableNetworkProgress;

public class NetworkRequestsModel extends ViewModel {

  @Getter
  private ObservableNetworkProgress onp = new ObservableNetworkProgress();
  @Getter // remove getter in the future
  private ViewModelCache vmCache = ViewModelCacheSingleton.getInstance();

  @Getter // remove getter in the future
  private DataUpdater dataUpdater = DataUpdater.builder()
      .observableNetworkProgress(onp)
      .cache(vmCache)
      .build();

}
