package com.example.kurshop

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.kurshop.ui.admin.KategoriFragment
import com.example.kurshop.ui.admin.ProdukAdminFragment
import com.example.kurshop.ui.admin.TransaksiFragment
import com.example.kurshop.ui.admin.UsersFragment

class AdminPagerAdapter(activity: AppCompatActivity) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ProdukAdminFragment()
            1 -> TransaksiFragment()
            2 -> KategoriFragment()
            3 -> UsersFragment()
            else -> ProdukAdminFragment()
        }
    }
}