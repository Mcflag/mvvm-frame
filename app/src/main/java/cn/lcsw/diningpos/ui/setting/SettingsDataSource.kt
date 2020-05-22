package cn.lcsw.diningpos.ui.setting

import cn.lcsw.diningpos.http.service.ServiceManager
import cn.lcsw.diningpos.manager.PrefsHelper
import cn.lcsw.mvvm.base.repository.BaseRepositoryBoth
import cn.lcsw.mvvm.base.repository.ILocalDataSource
import cn.lcsw.mvvm.base.repository.IRemoteDataSource
import io.reactivex.Completable

interface ISettingsRemoteDataSource : IRemoteDataSource {

}

interface ISettingsLocalDataSource : ILocalDataSource {
    fun savePrefsPayStyle(style: String): Completable
}

class SettingsDataSourceRepository(
    remoteDataSource: ISettingsRemoteDataSource,
    localDataSource: ISettingsLocalDataSource
) : BaseRepositoryBoth<ISettingsRemoteDataSource, ISettingsLocalDataSource>(remoteDataSource, localDataSource) {
    fun savePayStyle(style: String) = localDataSource.savePrefsPayStyle(style)
}

class SettingsRemoteDataSource(
    private val serviceManager: ServiceManager
) : ISettingsRemoteDataSource {

}

class SettingsLocalDataSource(
    private val prefs: PrefsHelper
) : ISettingsLocalDataSource {
    override fun savePrefsPayStyle(style: String): Completable =
        Completable.fromAction {
            prefs.payStyle = style
        }
}