package cn.lcsw.diningpos.ui.main

import cn.lcsw.diningpos.http.service.ServiceManager
import cn.lcsw.diningpos.manager.PrefsHelper
import cn.lcsw.mvvm.base.repository.BaseRepositoryBoth
import cn.lcsw.mvvm.base.repository.ILocalDataSource
import cn.lcsw.mvvm.base.repository.IRemoteDataSource

interface IMainRemoteDataSource : IRemoteDataSource

interface IMainLocalDataSource : ILocalDataSource

class MainDataSourceRepository(
    remoteDataSource: IMainRemoteDataSource,
    localDataSource: IMainLocalDataSource
) : BaseRepositoryBoth<IMainRemoteDataSource, IMainLocalDataSource>(remoteDataSource, localDataSource)

class MainRemoteDataSource(
    private val serviceManager: ServiceManager,
    private val prefs: PrefsHelper
) : IMainRemoteDataSource

class MainLocalDataSource(
    private val prefs: PrefsHelper
) : IMainLocalDataSource