package com.guidovezzoni.repofactory;

import com.guidovezzoni.architecture.cache.TimeStampHelper;
import com.guidovezzoni.architecture.cacheddatasource.MemoryCacheDataSource;
import com.guidovezzoni.architecture.datasource.DataSource;
import com.guidovezzoni.architecture.datasource.RetrofitFunctionDataSource;
import com.guidovezzoni.architecture.repository.NoCacheRepository;
import com.guidovezzoni.architecture.repository.Repository;
import com.guidovezzoni.architecture.repository.SingleLevelCacheRepository;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import kotlin.jvm.functions.Function1;
import retrofit2.Response;

@SuppressWarnings({"WeakerAccess", "unused"})
public class RepoFactory {
    public final TimeStampHelper timeStampHelper;

    public RepoFactory() {
        timeStampHelper = createTimeStampHelper();
    }

    protected TimeStampHelper createTimeStampHelper() {
        return new TimeStampHelper(System::currentTimeMillis);
    }

    protected <M, P> DataSource<M, P> createNetworkDataSource(@NonNull Function1<P, Single<Response<M>>> endPointGet) {
        return new RetrofitFunctionDataSource<>(timeStampHelper, endPointGet);
    }

    protected <M, P> MemoryCacheDataSource<M, P> createCachedSource() {
        return new MemoryCacheDataSource<>(timeStampHelper);
    }

    public <M, P> Repository<M, P> createRepo(RepoType repoType, @NonNull Function1<P, Single<Response<M>>> endPointGet) {
        DataSource<M, P> networkSource = createNetworkDataSource(endPointGet);

        switch (repoType) {
            case NO_CACHE:
                return new NoCacheRepository<>(networkSource);
            case SINGLE_CACHE:
                MemoryCacheDataSource<M, P> cachedDataSource = createCachedSource();
                return new SingleLevelCacheRepository<>(networkSource, cachedDataSource);
            default:
                throw new IllegalArgumentException("repo type not defined");
        }
    }
}
