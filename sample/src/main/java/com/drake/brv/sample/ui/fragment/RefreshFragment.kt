/*
 * Copyright (C) 2018 Drake, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.drake.brv.sample.ui.fragment

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import com.drake.brv.sample.R
import com.drake.brv.sample.databinding.FragmentRefreshBinding
import com.drake.brv.sample.model.Model
import com.drake.brv.sample.model.TwoSpanModel
import com.drake.brv.utils.*
import com.drake.engine.base.EngineFragment
import com.drake.engine.databinding.bind
import com.drake.tooltip.toast
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class RefreshFragment : EngineFragment<FragmentRefreshBinding>(R.layout.fragment_refresh) {

    private var curType = 1
    private var page = 1
    private val simpleFlow = MutableSharedFlow<List<Any>>(replay = 1)

    override fun initView() {
        setHasOptionsMenu(true)

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.rb1) {
                curType = 1
                binding.page.refresh()
            } else if (checkedId == R.id.rb2) {
                curType = 2
                binding.page.refresh()
            }
        }

        binding.btnSearch.setOnClickListener {
            binding.page.refresh()
        }

        binding.rv.linear().setup {
            addType<Model>(R.layout.item_multi_type_simple)
            addType<TwoSpanModel>(R.layout.item_multi_type_two_span)
        }

        binding.page
            .onRefresh {
                getData()
            }
            .onLoadMore {
                getData(false)
            }

        lifecycleScope
            .launch {
                simpleFlow.collectLatest {
                    if (page == 1) {
                        binding.rv.models = it
                    } else {
                        binding.rv.addModels(it)
                    }
                    binding.page.showContent(it.size >= 10)
                }
            }

        binding.page.refresh()
    }

    private fun getData(refresh: Boolean = true) {
        if (refresh) {
            page = 1
        } else {
            page ++
        }
        val searchKey = binding.editText.text.toString()
        val data = getDataByPage(page, searchKey, curType)
        simpleFlow.tryEmit(data)
    }

    private fun getDataByPage(page: Int, search: String = "", type: Int = 1): List<Any> {
        toast("获取第 $page 页数据, 搜索 >> $search")
        return mutableListOf<Any>().apply {
            if (type == 1) {
                for (i in 0..9) {
                    when (i) {
                        1, 2 -> add(TwoSpanModel())
                        else -> add(Model())
                    }
                }
            } else {
                repeat(4) {
                    add(Model())
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_refresh, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_loading -> binding.page.showLoading()  // 加载中
            R.id.menu_pull_refresh -> binding.page.autoRefresh() // 下拉刷新
            R.id.menu_refresh -> binding.page.refresh() // 静默刷新
            R.id.menu_content -> binding.page.showContent() // 加载成功
            R.id.menu_error -> binding.page.showError(force = true) // 强制加载错误
            R.id.menu_empty -> binding.page.showEmpty() // 空数据
            R.id.menu_refresh_success -> binding.page.finish() // 刷新成功
            R.id.menu_refresh_fail -> binding.page.finish(false) // 刷新失败
            R.id.menu_no_load_more -> binding.page.finishLoadMoreWithNoMoreData() // 没有更多数据
        }
        return super.onOptionsItemSelected(item)
    }

    override fun initData() {
    }
}
