/*
 * Copyright (C) 2016 Mantas Varnagiris.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package com.mvcoding.expensius

import android.content.Context
import android.preference.PreferenceManager
import com.memoizrlabs.Shank.registerFactory
import com.memoizrlabs.ShankModule
import com.memoizrlabs.shankkotlin.provideGlobalSingleton
import com.memoizrlabs.shankkotlin.provideNew
import com.memoizrlabs.shankkotlin.registerFactory
import com.mvcoding.expensius.extensions.interval
import com.mvcoding.expensius.feature.AmountFormatter
import com.mvcoding.expensius.feature.DateFormatter
import com.mvcoding.expensius.feature.Filter
import com.mvcoding.expensius.feature.currency.provideCurrencyFormatsProvider
import com.mvcoding.expensius.firebase.FirebaseAppUserService
import com.mvcoding.expensius.firebase.FirebaseAppUserWriteService
import com.mvcoding.expensius.firebase.FirebaseTagsService
import com.mvcoding.expensius.firebase.FirebaseTagsWriteService
import com.mvcoding.expensius.firebase.FirebaseTransactionsService
import com.mvcoding.expensius.firebase.FirebaseTransactionsWriteService
import com.mvcoding.expensius.model.ReportPeriod.MONTH
import com.mvcoding.expensius.provider.database.DBHelper
import com.mvcoding.expensius.provider.database.Database
import com.mvcoding.expensius.provider.database.SqliteDatabase
import com.mvcoding.expensius.provider.database.table.TagsTable
import com.mvcoding.expensius.provider.database.table.TransactionTagsTable
import com.mvcoding.expensius.provider.database.table.TransactionsTable
import com.mvcoding.expensius.service.AppUserService
import com.mvcoding.expensius.service.AppUserWriteService
import com.mvcoding.expensius.service.LoginService
import com.mvcoding.expensius.service.TagsService
import com.mvcoding.expensius.service.TagsWriteService
import com.mvcoding.expensius.service.TransactionsService
import com.mvcoding.expensius.service.TransactionsWriteService
import com.squareup.sqlbrite.SqlBrite
import rx.android.schedulers.AndroidSchedulers.mainThread
import rx.schedulers.Schedulers.computation
import rx.schedulers.Schedulers.io

class AppModule(val context: Context) : ShankModule {

    override fun registerFactories() {
        appContext()
        rxSchedulers()
        dateFormatter()
        amountFormatter()
        filter()

        firebaseAppUserService()
        firebaseTagsService()
        firebaseTagsWriteService()
        firebaseTransactionsService()
        firebaseTransactionsWriteService()
        firebaseAppUserWriteService()

        rxBus()
        settings()
        database()
    }

    private fun filter() = registerFactory(Filter::class) { ->
        val reportPeriod = MONTH
        Filter().setInterval(reportPeriod.interval(provideTimestampProvider().currentTimestamp()))
    }


    private fun appContext() = registerFactory(Context::class) { -> context }
    private fun rxSchedulers() = registerFactory(RxSchedulers::class) { -> RxSchedulers(mainThread(), io(), computation()) }
    private fun firebaseAppUserService() = registerFactory(FirebaseAppUserService::class) { -> FirebaseAppUserService() }
    private fun firebaseTagsService() = registerFactory(FirebaseTagsService::class) { archived: Boolean ->
        FirebaseTagsService(provideAppUserService(), archived)
    }

    private fun firebaseTagsWriteService() = registerFactory(FirebaseTagsWriteService::class) { -> FirebaseTagsWriteService(provideAppUserService()) }

    private fun firebaseTransactionsService() = registerFactory(FirebaseTransactionsService::class) { archived: Boolean ->
        FirebaseTransactionsService(provideAppUserService(), archived, provideTagsService(), provideArchivedTagsService())
    }

    private fun firebaseTransactionsWriteService() = registerFactory(FirebaseTransactionsWriteService::class) { ->
        FirebaseTransactionsWriteService(provideAppUserService())
    }

    private fun firebaseAppUserWriteService() = registerFactory(FirebaseAppUserWriteService::class) { -> FirebaseAppUserWriteService(provideAppUserService()) }

    private fun rxBus() = registerFactory(RxBus::class) { -> RxBus() }
    private fun dateFormatter() = registerFactory(DateFormatter::class) { -> DateFormatter(context) }

    private fun settings() {
        registerFactory(Settings::class.java, { -> UserSettings(PreferenceManager.getDefaultSharedPreferences(provideContext())) })
    }

    private fun database() {
        val briteDatabase = SqlBrite.create().wrapDatabaseHelper(DBHelper(
                context,
                TagsTable(),
                TransactionsTable(),
                TransactionTagsTable()),
                io())
        registerFactory(Database::class.java, { -> SqliteDatabase(briteDatabase) })
    }

    private fun amountFormatter() {
        registerFactory(AmountFormatter::class.java) { -> AmountFormatter(provideCurrencyFormatsProvider()) }
    }
}

fun provideContext() = provideGlobalSingleton<Context>()
fun provideRxSchedulers() = provideGlobalSingleton<RxSchedulers>()
fun provideRxBus() = provideGlobalSingleton<RxBus>()
fun provideSettings() = provideGlobalSingleton<Settings>()
fun provideDatabase() = provideGlobalSingleton<Database>()
fun provideDateFormatter() = provideGlobalSingleton<DateFormatter>()
fun provideAmountFormatter() = provideGlobalSingleton<AmountFormatter>()
fun provideTimestampProvider() = SystemTimestampProvider()
fun provideFilter() = provideNew<Filter>()

fun provideAppUserService(): AppUserService = provideFirebaseAppUserService()
fun provideLoginService(): LoginService = provideFirebaseAppUserService()
fun provideTagsService(): TagsService = provideFirebaseTagsService(false)
fun provideArchivedTagsService(): TagsService = provideFirebaseTagsService(true)
fun provideTagsWriteService(): TagsWriteService = provideFirebaseTagsWriteService()
fun provideTransactionsService(): TransactionsService = provideFirebaseTransactionsService(false)
fun provideArchivedTransactionsService(): TransactionsService = provideFirebaseTransactionsService(true)
fun provideTransactionsWriteService(): TransactionsWriteService = provideFirebaseTransactionsWriteService()
fun provideAppUserWriteService(): AppUserWriteService = provideFirebaseAppUserWriteService()

private fun provideFirebaseAppUserService() = provideGlobalSingleton<FirebaseAppUserService>()
private fun provideFirebaseTagsService(archived: Boolean) = provideGlobalSingleton<FirebaseTagsService>(archived)
private fun provideFirebaseTagsWriteService() = provideNew<FirebaseTagsWriteService>()
private fun provideFirebaseTransactionsService(archived: Boolean) = provideGlobalSingleton<FirebaseTransactionsService>(archived)
private fun provideFirebaseTransactionsWriteService() = provideNew<FirebaseTransactionsWriteService>()
private fun provideFirebaseAppUserWriteService() = provideNew<FirebaseAppUserWriteService>()
