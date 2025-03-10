"use client";

import React, { useState } from "react";
import { Search } from "lucide-react";

const SearchBar = () => {
  const [searchTerm, setSearchTerm] = useState("");

  return (
    <div className="relative w-full max-w-md">
      <input
        type="text"
        placeholder="어떤 CS 주제를 벼락치기 할까요?"
        className="bg-background border border-border font-muted w-full p-2 pl-8 rounded-full shadow-md focus:outline-none focus:ring-2"
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
      />
      <Search
        className="absolute left-2 top-1/2 transform -translate-y-1/2 bg-background"
        size={16}
      />
    </div>
  );
};

export default SearchBar;
